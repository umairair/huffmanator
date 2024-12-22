import java.io.*;
import java.util.ArrayList;

import net.datastructures.*;

/**
 * Class Huffman that provides Huffman compression encoding and decoding of files
 * 
 *
 */
public class Huffman {

    /**
	 * 
	 * Inner class Huffman Node to Store a node of Huffman Tree
	 *
	 */
    private class HuffmanTreeNode {
        private int character;
        private int count;
        private HuffmanTreeNode left;
        private HuffmanTreeNode right;

        public HuffmanTreeNode(int c, int ct, HuffmanTreeNode leftNode, HuffmanTreeNode rightNode) {
            character = c;
            count = ct;
            left = leftNode;
            right = rightNode;
        }

        public int getChar() { return character; }
        public Integer getCount() { return count; }
        public HuffmanTreeNode getLeft() { return left; }
        public HuffmanTreeNode getRight() { return right; }
        public boolean isLeaf() { return left == null; }
    }

    /**
	 * 
	 * Auxiliary class to write bits to an OutputStream
	 * Since files output one byte at a time, a buffer is used to group each output of 8-bits
	 * Method close should be invoked to flush half filed buckets by padding extra 0's
	 */
    private class OutBitStream {
        OutputStream out;
        int buffer;
        int buffCount;

        public OutBitStream(OutputStream output) {
            out = output;
            buffer = 0;
            buffCount = 0;
        }

        public void writeBit(int i) throws IOException {
            buffer = (buffer << 1) | i;
            buffCount++;
            if (buffCount == 8) {
                out.write(buffer);
                buffCount = 0;
                buffer = 0;
            }
        }

        public void close() throws IOException {
            if (buffCount > 0) {
                buffer = buffer << (8 - buffCount);
                out.write(buffer);
            }
            out.close();
        }
    }

	/**
	 * 
	 * Auxiliary class to read bits from a file
	 * Since we must read one byte at a time, a buffer is used to group each input of 8-bits
	 * 
	 */
    private class InBitStream {
        InputStream in;
        int buffer;
        int buffCount;

        public InBitStream(InputStream input) {
            in = input;
            buffer = 0;
            buffCount = 8;
        }

        public int readBit() throws IOException {
            if (buffCount == 8) {
                buffCount = 0;
                buffer = in.read();
                if (buffer == -1) return -1;
            }
            int bit = (buffer >> (7 - buffCount)) & 1;
            buffCount++;
            return bit;
        }
    }

	/**
	 * Builds a frequency table indicating the frequency of each character/byte in the input stream
	 * @param input is a file where to get the frequency of each character/byte
	 * @return freqTable a frequency table must be an ArrayList<Integer? such that freqTable.get(i) = number of times character i appears in file 
	 *                   and such that freqTable.get(256) = 1 (adding special character representing"end-of-file")
	 * @throws IOException indicating errors reading input stream
	 */
    private ArrayList<Integer> buildFrequencyTable(InputStream input) throws IOException {
        ArrayList<Integer> freqTable = new ArrayList<>(257);
        for (int i = 0; i < 257; i++) {
            freqTable.add(0);
        }

        InBitStream ibs = new InBitStream(input);
        int ascii;

        while (true) {
            ascii = 0;
            boolean endOfFile = false;

            for (int i = 0; i < 8; i++) {
                int bit = ibs.readBit();
                if (bit == -1) {
                    endOfFile = true;
                    break;
                }
                ascii = (ascii << 1) | bit; 
            }

            if (endOfFile) {
                break;
            }

            freqTable.set(ascii, freqTable.get(ascii) + 1);
        }

        freqTable.set(256, 1);
       
        return freqTable;
    }

	/**
	 * Create Huffman tree using the given frequency table; the method requires a heap priority queue to run in O(nlogn) where n is the characters with nonzero frequency
	 * @param freqTable the frequency table for characters 0..255 plus 256 = "end-of-file" with same specs are return value of buildFrequencyTable
	 * @return root of the Huffman tree build by this method
	 */
    private HuffmanTreeNode buildEncodingTree(ArrayList<Integer> freqTable) {
        HeapPriorityQueue<Integer, HuffmanTreeNode> pq = new HeapPriorityQueue<>();
        for (int i = 0; i < 257; i++) {
            int frequency = freqTable.get(i);
            if (frequency > 0) {
                pq.insert(frequency, new HuffmanTreeNode(i, frequency, null, null));
            }
        }

        while (pq.size() > 1) {
            Entry<Integer, HuffmanTreeNode> leftEntry = pq.removeMin();
            Entry<Integer, HuffmanTreeNode> rightEntry = pq.removeMin();
            int combinedFrequency = leftEntry.getKey() + rightEntry.getKey();
            HuffmanTreeNode parentNode = new HuffmanTreeNode(-1, combinedFrequency, leftEntry.getValue(), rightEntry.getValue());
            pq.insert(combinedFrequency, parentNode);
        }

        return pq.isEmpty() ? null : pq.removeMin().getValue();
    }

    /**
	 * 
	 * @param encodingTreeRoot - input parameter storing the root of the HUffman tree
	 * @return an ArrayList<String> of length 257 where code.get(i) returns a String of 0-1 correspoding to each character in a Huffman tree
	 * code.get(i) returns null if i is not a leaf of the Huffman tree
	 */
    private static ArrayList<String> buildEncodingTable(HuffmanTreeNode encodingTreeRoot) {
        ArrayList<String> code = new ArrayList<>(257);
        for (int i = 0; i < 257; i++) {
            code.add(null);
        }
        buildEncodingTableRecursion(encodingTreeRoot, "", code);


        return code;

    }

    /**
	 * Recursively traverses the huffman tree to build a table where each index corresponds to an ascii key, and the value at said index is the huffman code found by traversing the tree
	 * @param node - input parameter storing the root of the Huffman tree
	 * @param path - starting string to append bit's to
     * @param code -  ArrayList<String> of length 257 where code.get(i) returns a String of 0-1 correspoding to each character in a Huffman tree
	 * code.get(i) returns null if i is not a leaf of the Huffman tree
	 */
    private static void buildEncodingTableRecursion(HuffmanTreeNode node, String path, ArrayList<String> code) {
        if (node == null) {
            return;
        }

        if (node.isLeaf() && node.getChar() != -1) {
            code.set(node.getChar(), path);
        } else {
            buildEncodingTableRecursion(node.getLeft(), path + "0", code);
            buildEncodingTableRecursion(node.getRight(), path + "1", code);
        }
    }

    /**
	 * Encodes an input using encoding Table that stores the Huffman code for each character
	 * @param input - input parameter, a file to be encoded using Huffman encoding
	 * @param encodingTable - input parameter, a table containing the Huffman code for each character
	 * @param output - output paramter - file where the encoded bits will be written to.
	 * @throws IOException indicates I/O errors for input/output streams
	 */
    private void encodeData(InputStream input, ArrayList<String> encodingTable, OutputStream output) throws IOException {
        OutBitStream bitStream = new OutBitStream(output);
        InBitStream ibs = new InBitStream(input);
        int ascii;
        boolean endOfFile = false;
        int count = 0;
        while (!endOfFile) {
            ascii = 0;
            for (int i = 0; i < 8; i++) {
                int bit = ibs.readBit();
                if (bit == -1) {
                    endOfFile = true;
                    break;
                }
                ascii = (ascii << 1) | bit;
            }
    
            if (!endOfFile) {
                String huffSequence = encodingTable.get(ascii);
                
                if (huffSequence != null) {
                    for (int i = 0; i < huffSequence.length(); i++) {
                        bitStream.writeBit(huffSequence.charAt(i) - '0');
                        count +=1;
                       
                    }
                }
            }
   
        }
        
        String eof = encodingTable.get(256);
        if (eof != null) {
            for (int i = 0; i < eof.length(); i++) {
                bitStream.writeBit(eof.charAt(i) - '0');
                count += 1;
            }
        }
        
        System.out.println("Number of bits in output: " +Math.round(((Math.ceil(count))/8)) +"\n");
        bitStream.close();
    }

	/**
	 * Decodes an encoded input using encoding tree, writing decoded file to output
	 * @param input  input parameter a stream where header has already been read from
	 * @param encodingTreeRoot input parameter contains the root of the Huffman tree
	 * @param output output parameter where the decoded bytes will be written to 
	 * @throws IOException indicates I/O errors for input/output streams
	 */
    private void decodeData(ObjectInputStream input, HuffmanTreeNode encodingTreeRoot, FileOutputStream output) throws IOException {
        InBitStream inputBitStream = new InBitStream(input); // Associates a bit stream to read bits from the file
        ArrayList<String> codeTable = buildEncodingTable(encodingTreeRoot); // Get the Huffman code table
        int count = 0;
        String currentCode = "";

        while(true) {
                int bit = inputBitStream.readBit();
                count++;
                if(bit == -1) {
                    break;
                }
               currentCode += bit +"";
            
               for (int i = 0; i < codeTable.size() - 1; i++) {
                    if (codeTable.get(i) != null && codeTable.get(i).equals(currentCode)) {
                        output.write(i); 
                        currentCode = "";
                        break; 
                    }
                }
            }
    
     System.out.println("Number of bytes in input: " +Math.round(((Math.ceil(count))/8)));
    
    }

	/**
	 * Counts the bytes in a file givn the file path
	 * @param filePath  path of the file whose bytes will be counted
	 * @throws IOException indicates I/O errors for input/output streams
	 */
    public long countBytesInFile(String filePath) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            long byteCount = 0;
            while (inputStream.read() != -1) {
                byteCount++;
            }
            return byteCount;
        }
    }

	/**
	 * Method that implements Huffman encoding on plain input into encoded output
	 * @param input - this is the file to be encoded (compressed)
	 * @param codedOutput - this is the Huffman encoded file corresponding to input
	 * @throws IOException indicates problems with input/output streams
	 */
    public void encode(String inputFileName, String outputFileName) throws IOException {
        System.out.println("\nEncoding " + inputFileName + " " + outputFileName);
        try (FileInputStream input = new FileInputStream(inputFileName);
             FileInputStream copyinput = new FileInputStream(inputFileName);
             FileOutputStream out = new FileOutputStream(outputFileName);
             ObjectOutputStream codedOutput = new ObjectOutputStream(out)) {

            System.out.println("Number of bytes in input: " + countBytesInFile(inputFileName));
            ArrayList<Integer> freqTable = buildFrequencyTable(input);
           // System.out.println("FrequencyTable is="+freqTable);

            HuffmanTreeNode root = buildEncodingTree(freqTable);
            ArrayList<String> codes = buildEncodingTable(root);
           // System.out.println("EncodingTable is="+codes);

            codedOutput.writeObject(freqTable);
            encodeData(copyinput, codes, codedOutput);
        }
    }

     /**
     * Method that implements Huffman decoding on encoded input into a plain output
     * @param codedInput  - this is an file encoded (compressed) via the encode algorithm of this class 
     * @param output      - this is the output where we must write the decoded file  (should original encoded file)
     * @throws IOException - indicates problems with input/output streams
     * @throws ClassNotFoundException - handles case where the file does not contain correct object at header
     */
    public void decode(String inputFileName, String outputFileName) throws IOException, ClassNotFoundException {
        try (FileInputStream in = new FileInputStream(inputFileName);
             ObjectInputStream codedInput = new ObjectInputStream(in);
             FileOutputStream output = new FileOutputStream(outputFileName)) {

            @SuppressWarnings("unchecked")
            ArrayList<Integer> freqTable = (ArrayList<Integer>) codedInput.readObject();
            HuffmanTreeNode root = buildEncodingTree(freqTable);
            //System.out.println("FrequencyTable is="+freqTable);

            decodeData(codedInput, root, output);

            System.out.println("Number of bytes in output: " + countBytesInFile(outputFileName) + "\n");
        }
    }
}
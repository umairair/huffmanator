import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.stream.Stream;
import java.nio.file.Files;

/**
 * Class TestCompression to Test Huffman
 * @author Lucia Moura
 *
 */
public class TestCompression {


   /**
    * Helper method to take inputs from command line 
    * 
    * @param args contains commands in 3 types of format:
    *             E inputfile outputfile 
    *                  Usage example: java TestCompression E genes.txt genes.huf
    *                  E is for encoding inputfile into outputfile
    *             D inputfile outputfile
    *                  Usage example: java TestCompression D genes.huf genesRecover.txt
    *                  D is for decoding inputfile into outputfile
    *             T testfile
    *                  Usage example: java TestCompression T tests.txt
    *                  T goes to file that contains lines of type E and D one per line
    *                  we provided 'tests.txt' with the assignment
    *             
    * @throws IOException 
    * @throws ClassNotFoundException 
    */
   private static void testInput(String[] args) throws IOException, ClassNotFoundException {
   	Huffman myHuff=new Huffman();
   	
   	if (args.length <2) { System.out.println("Usage: TestCompression E/D inputfile outputfile\nUsage: TestCompression T testfile");
   	                        return;
   	}
   	switch (args[0]) {
   	   case "E": case "e": 
   		   myHuff.encode(args[1], args[2]);
   		   //System.out.println("Encoding complete");
           break;
   	   case "D": case "d":
   		   myHuff.decode(args[1], args[2]);
   		   //System.out.println("Decoding complete");
   	       break;
   	   case "T": case "t":
   		   Path path = Paths.get(args[1]);
   	       try (Stream<String> line = Files.lines(path)) {
   	             line.forEach(row -> {try {
					testInput(row.split(" "));
				} catch (Exception e) {
					System.out.println("Error in split");
				}
   	             });
   	       } catch (IOException e) {
   	           System.out.println("Cannot find file.");
   	       }
   	       System.out.println("Test file was completed.");
   	       break;
            
   	     default: System.out.println("Error: first argument must be E, D or T.");
   	     return;
    }
	
   	
   }



public static void main(String[] args) throws IOException, ClassNotFoundException { 
  	 Huffman myHuff=new Huffman();
  	 
  	 if (args.length==0) { // hardcoded and interactive inputs
  		 // hardcoded test for file genes.txt
  		 myHuff.encode("genes.txt", "genes.huf");
  		 myHuff.decode("genes.huf", "genesRecovered.txt");
  		 
  		 // interactive part typed as input
  		 while (true) { 
           System.out.println("\nCommand Formats:");
           System.out.println("E <inputfile> <outputfile>");
           System.out.println("D <inputfile> <outputfile>");
           System.out.println("T <testfile_with_commands>");
           System.out.println("or type Q for quiting\n");
           System.out.print("Enter command > ");
           Scanner input = new Scanner(System.in);
           String command=input.nextLine();
           if (command.equals("Q")) break;
           testInput(command.split(" "));
  		 }
         System.out.println("Ended program.");
  		 
  	 }
  	 else // command line inputs
  		 testInput(args);
 	 
  }




}

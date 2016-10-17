// Microprocessor Simulator
// A.Greensted - University of York
// ajg112@ohm.york.ac.uk
// November 2008

package sim;

import java.io.*;
import java.util.*;

public class Assembler
{
	public static Instruction[] assemble(File asmFile, PrintStream log, boolean verbose)
	{
		BufferedReader reader = null;
		Hashtable<String, Integer> labelTable = new Hashtable<String, Integer>();

		// PASS 1
		// ---------------------------------------------------
		// Find labels and add label and address to labelTable
		// ---------------------------------------------------
		try
		{
			reader = new BufferedReader(new FileReader(asmFile));
		}
		catch (FileNotFoundException fnfE)
		{
			log.println("ERR: Assembler.assemble: Could not open file");
			return null;
		}

		boolean errorFound = false;
		int address = 0;
		int lineNo = 0;

		while (true)
		{
			lineNo ++;
			String line = null;

			try
			{
				line = reader.readLine();
			}
			catch(IOException ioE)
			{
				errorFound = true;
				log.println("ERR: Assembler.assemble: (Pass 1) IOException reading assembler file (Line no: " + lineNo +")");
				break;
			}

			if (line == null) break;

			int commentPos = line.indexOf(";");
			if (commentPos != -1) line = line.substring(0, commentPos);
			line = line.trim();

			if (line.length() == 0) continue;

			int count = 0;
			boolean labelLine = false;

			// Split line at whitespace
			String[] symbols = line.split("\\s+");

			for(String symbol : symbols)
			{
				if (labelLine)
				{
					errorFound = true;
					log.println("ERR: Assembler.assemble: (Pass 1) Label should alone on a line (Line no: " + lineNo +")");
				}
				else if (symbol.endsWith(":"))
				{
					labelLine = true;

					if (count != 0)
					{
						errorFound = true;
						log.println("ERR: Assembler.assemble: (Pass 1) Label should be first argument on a line (Line no: " + lineNo +")");
					}
					else if (symbol.length() < 3)
					{
						errorFound = true;
						log.println("ERR: Assembler.assemble: (Pass 1) Label too short (Line no: " + lineNo + ")");
					}
					else
					{
						String label = symbol.substring(0, symbol.length()-1);

						if (labelTable.containsKey(label))
						{
							errorFound = true;
							log.println("ERR: Assembler.assemble: (Pass 1) Label duplicate (Line no: " + lineNo + ")");
						}
						else
						{
							labelTable.put(label, address);
						}
					}
				}
				else
				{
					count ++;
				}
			}

			// Increment the address if the line contained instructions
			if (count != 0) address ++;
		}

		try
		{
			reader.close();
		}
		catch(IOException ioE)
		{
			log.println("ERR: Assembler.assemble: (Pass 1) IOException when closing reader");
			return null;
		}

		if (errorFound)
		{
			log.println("ERR: Assembler.assemble: (Pass 1) Syntax errors found");
			return null;
		}

		// Display Contents of Label Table
		if (verbose)
		{
			if (labelTable.size() == 0)
			{
				log.println("Assembler.assemble: (Pass 1) No labels found");
			}
			else
			{
				log.println("Assembler.assemble: (Pass 1) Label Table:");
				Enumeration<String> e = labelTable.keys();
				while (e.hasMoreElements())
				{
					String label = e.nextElement();
					int add = labelTable.get(label);
					log.println("  " + label + ": " + add);
				}
			}
		}


		// PASS 2
		// ------
		try
		{
			reader = new BufferedReader(new FileReader(asmFile));
		}
		catch (FileNotFoundException fnfE)
		{
			log.println("ERR: Assembler.assemble: Could not open file");
			return null;
		}

		ArrayList<Instruction> insnList = new ArrayList<Instruction>();
		errorFound = false;
		address = 0;
		lineNo = 0;

		while (true)
		{
			lineNo ++;
			String line = null;

			try
			{
				line = reader.readLine();
			}
			catch(IOException ioE)
			{
				errorFound = true;
				log.println("ERR: Assembler.assemble: (Pass 2) IOException reading assembler file (Line no: " + lineNo +")");
				break;
			}

			if (line == null) break;

			int commentPos = line.indexOf(";");
			if (commentPos != -1) line = line.substring(0, commentPos);
			line = line.trim();

			if (line.length() == 0) continue;

			// Split line at whitespace
			String[] symbols = line.split("\\s+");

			// Ignore label lines
			if (symbols[0].endsWith(":")) continue;

			String opString = symbols[0];
			String argStrings[] = new String[symbols.length-1];
			for (int s=0 ; s<symbols.length-1 ; s++) argStrings[s] = symbols[s+1];

			Instruction insn = parseInsn(opString, argStrings, labelTable, lineNo, log);
			if (insn == null) errorFound = true;
			else insnList.add(insn);
		}

		try
		{
			reader.close();
		}
		catch(IOException ioE)
		{
			log.println("ERR: Assembler.assemble: (Pass 1) IOException when closing reader");
			return null;
		}

		if (errorFound)
		{
			log.println("ERR: Assembler.assemble: (Pass 2) Errors occured");
			return null;
		}

		Instruction insnArray[] = new Instruction[insnList.size()];
		insnArray = insnList.toArray(insnArray);
		
		if (verbose) log.println("Assembler.assemble: Assembled " + insnArray.length + " instructions");

		return insnArray;
	}

	private static Instruction parseInsn(String opStr, String[] argStrings, Hashtable<String, Integer> labelTable, int lineNo, PrintStream log)
	{
		boolean argError = false;

		// Parse Argument
		Integer[] argValArray = new Integer[argStrings.length];
		Instruction.ArgType[] argTypeArray = new Instruction.ArgType[argStrings.length];

//		String srcString = "";

		for (int a=0 ; a<argStrings.length ; a++)
		{
			if (argStrings[a] != null)
			{
//				srcString += " " + argStrings[a];

				argValArray[a] = Parser.parseRegister(argStrings[a]);
				if (argValArray[a] != null) argTypeArray[a] = Instruction.ArgType.REG;
				else
				{
					argValArray[a] = Parser.parseImmediate(argStrings[a]);
					if (argValArray[a] != null) argTypeArray[a] = Instruction.ArgType.IMM;
					else
					{
						argValArray[a] = labelTable.get(argStrings[a]);
						if (argValArray[a] != null) argTypeArray[a] = Instruction.ArgType.IMM;
						else
						{
							log.println("Assembler.parseInsn: Bad argument, '" + argStrings[a] + "' (Line no: " + lineNo + ")");
							argError = true;
						}
					}
				}
			}
		}

		// Return if there was an error in the arguments
		if (argError) return null;

		InsnLoop: for (Instruction.InstructionType insnType : Instruction.InstructionType.values())
		{
			// Check for a opCode mnemonic match
			if (!opStr.equals(insnType.opCode)) continue;

			// Check for correct number of arguments
			if (argStrings.length != insnType.argTypeArray.length) continue;

			int encoding = (insnType.ordinal() << 12);

			String srcString = insnType.opCode;

			ArgLoop: for (int a=0 ; a<insnType.argTypeArray.length ; a++)
			{
				switch (insnType.argTypeArray[a])
				{
					case REG:
						if (argTypeArray[a] != Instruction.ArgType.REG) continue InsnLoop;
						if (argValArray[a] < 0 || argValArray[a] > 15) continue InsnLoop;
						srcString += " r" + argValArray[a];
						break;

					case IMM:
						if (argTypeArray[a] != Instruction.ArgType.IMM) continue InsnLoop;
						if (argValArray[a] < 0 || argValArray[a] > 255) continue InsnLoop;
						srcString += " " + String.format("x%02X", argValArray[a]);
						break;

					case UADD:
						if (argTypeArray[a] != Instruction.ArgType.IMM) continue InsnLoop;
						if (argValArray[a] < 0 || argValArray[a] > 4095) continue InsnLoop;
						srcString += " " + String.format("x%03X", argValArray[a]);
						break;

					case SADD:
						if (argTypeArray[a] != Instruction.ArgType.IMM) continue InsnLoop;
						if (argValArray[a] < -2048 || argValArray[a] > 2047) continue InsnLoop;
						if (argValArray[a]<0) srcString += " -" + String.format("x%02X", argValArray[a]*-1);
						else srcString += " " + String.format("x%03X", argValArray[a]);
						break;
				}
				int arg = argValArray[a] & ((1 << insnType.argLenArray[a])-1);	// Mask
				encoding |= (arg << insnType.argPosArray[a]);						// Shift into position and set
			}

			int args[] = new int[argValArray.length];
			for (int a=0 ; a<argValArray.length ; a++) args[a] = argValArray[a];
		
	//		srcString = srcString += insnType.opCode;

			return new Instruction(insnType, encoding, args, lineNo, srcString);
		}

		log.println("Assembler.parseInsn: Bad instruction (Line no: " + lineNo + ")");
		return null;
	}
}

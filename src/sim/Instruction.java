// Microprocessor Simulator
// A.Greensted - University of York
// ajg112@ohm.york.ac.uk
// November 2008

package sim;

public class Instruction
{
	public enum ArgType {REG, IMM, UADD, SADD};

	public enum InstructionType {
		// Arithmetic
		ADDi	("add",	0,	new ArgType[] {ArgType.REG, ArgType.IMM},		new int[] {8, 0},	new int[] {4, 8}),
		ADDr	("add",	1,	new ArgType[] {ArgType.REG, ArgType.REG},		new int[] {8, 0},	new int[] {4, 4}),

//		ADDCi	("addc",	0,	new ArgType[] {ArgType.REG, ArgType.IMM},		new int[] {8, 0},	new int[] {4, 8}),
//		ADDCr	("addc",	1,	new ArgType[] {ArgType.REG, ArgType.REG},		new int[] {8, 0},	new int[] {4, 4}),

		SUBi	("sub",	0,	new ArgType[] {ArgType.REG, ArgType.IMM},		new int[] {8, 0},	new int[] {4, 8}),
		SUBr	("sub",	1,	new ArgType[] {ArgType.REG, ArgType.REG},		new int[] {8, 0},	new int[] {4, 4}),

//		SUBCi	("subc",	0,	new ArgType[] {ArgType.REG, ArgType.IMM},		new int[] {8, 0},	new int[] {4, 8}),
//		SUBCr	("subc",	1,	new ArgType[] {ArgType.REG, ArgType.REG},		new int[] {8, 0},	new int[] {4, 4}),

		COMPi	("comp",	0,	new ArgType[] {ArgType.REG, ArgType.IMM},		new int[] {8, 0},	new int[] {4, 8}),
		COMPr	("comp",	1,	new ArgType[] {ArgType.REG, ArgType.REG},		new int[] {8, 0},	new int[] {4, 4}),

//		NEGr	("neg",	1,	new ArgType[] {ArgType.REG},						new int[] {0},		new int[] {4}),

		// Logic
		ANDi	("and",	0,	new ArgType[] {ArgType.REG, ArgType.IMM},		new int[] {8, 0},	new int[] {4, 8}),
		ANDr	("and",	1,	new ArgType[] {ArgType.REG, ArgType.REG},		new int[] {8, 0},	new int[] {4, 4}),

		ORi	("or",	0,	new ArgType[] {ArgType.REG, ArgType.IMM},		new int[] {8, 0},	new int[] {4, 8}),
		ORr	("or",	1,	new ArgType[] {ArgType.REG, ArgType.REG},		new int[] {8, 0},	new int[] {4, 4}),

		XORi	("xor",	0,	new ArgType[] {ArgType.REG, ArgType.IMM},		new int[] {8, 0},	new int[] {4, 8}),
		XORr	("xor",	1,	new ArgType[] {ArgType.REG, ArgType.REG},		new int[] {8, 0},	new int[] {4, 4}),

//		ROTi	("rot",	0,	new ArgType[] {ArgType.REG, ArgType.IMM},		new int[] {8, 0},	new int[] {4, 8}),
//		ROTr	("rot",	1,	new ArgType[] {ArgType.REG, ArgType.REG},		new int[] {8, 0},	new int[] {4, 4}),

		SHLi	("shl",	0,	new ArgType[] {ArgType.REG, ArgType.IMM},		new int[] {8, 0},	new int[] {4, 8}),
		SHLr	("shl",	1,	new ArgType[] {ArgType.REG, ArgType.REG},		new int[] {8, 0},	new int[] {4, 4}),

		SHRi	("shr",	0,	new ArgType[] {ArgType.REG, ArgType.IMM},		new int[] {8, 0},	new int[] {4, 8}),
		SHRr	("shr",	1,	new ArgType[] {ArgType.REG, ArgType.REG},		new int[] {8, 0},	new int[] {4, 4}),

		// Moves
		MOVi	("mov",	0,	new ArgType[] {ArgType.REG, ArgType.IMM},		new int[] {8, 0},	new int[] {4, 8}),
		MOVr	("mov",	1,	new ArgType[] {ArgType.REG, ArgType.REG},		new int[] {8, 0},	new int[] {4, 4}),

		// Data Memory
		LDi	("ld",	0,	new ArgType[] {ArgType.REG, ArgType.IMM},		new int[] {8, 0},	new int[] {4, 8}),
		LDr	("ld",	1,	new ArgType[] {ArgType.REG, ArgType.REG},		new int[] {8, 0},	new int[] {4, 4}),

		STi	("st",	0,	new ArgType[] {ArgType.REG, ArgType.IMM},		new int[] {8, 0},	new int[] {4, 8}),
		STa	("st",	0,	new ArgType[] {ArgType.IMM, ArgType.REG},		new int[] {0, 8},	new int[] {8, 4}),
		STr	("st",	1,	new ArgType[] {ArgType.REG, ArgType.REG},		new int[] {8, 0},	new int[] {4, 4}),

//		IN		("shr",	new ArgType[] {ArgType.REG, ArgType.IMM},		new int[] {8, 0},	new int[] {4, 8}),
//		OUT	("shr",	new ArgType[] {ArgType.REG, ArgType.REG},		new int[] {8, 0},	new int[] {4, 4}),

//		CALL
//		RETURN
//		RETURNI

		// Jumps/Branches
		JMPi	("jmp",	2,	new ArgType[] {ArgType.UADD},						new int[] {0},		new int[] {12}),

//		JMPRi	("jmpr",	2,	new ArgType[] {ArgType.SADD},						new int[] {0},		new int[] {8}),
//		JMPRr	("jmpr",	3,	new ArgType[] {ArgType.REG},						new int[] {8},		new int[] {4}),

		BRZi	("brz",	2,	new ArgType[] {ArgType.UADD},						new int[] {0},		new int[] {12}),
		BRNZi	("brnz",	2, new ArgType[] {ArgType.UADD},						new int[] {0},		new int[] {12});

		public String opCode;
		public int format;
		public ArgType[] argTypeArray;
		public int[] argPosArray;
		public int[] argLenArray;

		InstructionType(String opCode, int format, ArgType[] argTypeArray, int[] argPosArray, int[] argLenArray)
		{
			this.opCode = opCode;
			this.format = format;
			this.argTypeArray = argTypeArray;
			this.argPosArray = argPosArray;
			this.argLenArray = argLenArray;
		}
	}

	public InstructionType insnType;
	public int encoding;
	public int[] argValArray;
	public int srcLine;
	public String srcString;

	public Instruction(InstructionType insnType, int encoding, int[] argValArray, int srcLine, String srcString)
	{
		this.insnType = insnType;
		this.encoding = encoding;
		this.argValArray = argValArray;
		this.srcLine = srcLine;
		this.srcString = srcString;
	}

	public String toString()
	{
		return srcString + " (" + StringUtils.intToBinaryString(encoding, 18, true) + ") (" + Integer.toString(srcLine) + ")";
	}

	public static InstructionType getType(int opCode)
	{
		return InstructionType.values()[opCode];
	}
}

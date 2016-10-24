// Microprocessor Simulator
// A.Greensted - University of York
// ajg112@ohm.york.ac.uk
// November 2008

package sim;

public class Processor
{
	public enum AluOp {ADD, ADDC, SUB, SUBC, AND, XOR, OR, OPA, OPB, ROL, SHL, SHR}

	// Options
	public boolean enableRegForwarding = true;
	public boolean enableJumpFlush = true;
	public boolean enableLoadStall = true;

	// Register File and Data Memory
	public int[] registerFile;
	public int[] dataMemory;

	public long clockCount;

	public int feInsnAdd;
	public int deInsnAdd;
	public int exInsnAdd;
	public int maInsnAdd;
	public int wbInsnAdd;

	public class Signals implements Cloneable
	{
		// FETCH
		// ------

		// reg
		public int fe_pc;

		// comb
		public int fe_pcPlus;

		// DECODE
		// ------

		// reg
		public int de_pc;
		public int de_feFlush = 1;
		public int de_opCode;
		public int de_rAddA;
		public int de_rAddB;
		public int de_imm;
		public int de_insn;

		// EXECUTE
		// -------

		// reg
		public int ex_pc;
		public int ex_deFlush = 1;
		public int ex_feFlush = 1;
		public int ex_opCode;
		public int ex_rAddA;
		public int ex_rAddB;
		public int ex_rDatA;
		public int ex_rDatB;
		public int ex_imm;

		// comb
		public int ex_opASE;
		public int ex_pcRel;
		public int ex_pcJmp;
		public int ex_nPC;
		public int ex_opA;
		public int ex_opB;
		public int ex_aluOut;
		public int ex_mAdd;

		public int ex_flagZ;
		public int ex_flagC;

		// MEMORY ACCESS
		// -------------

		// reg
		public int ma_wrEn;			// Register File Write Enable
		public int ma_wrAdd;
		public int ma_muxDS;
		public int ma_aluOut;
		public int ma_mAdd;
		public int ma_mEn;

		public int ma_flagZ;
		public int ma_flagC;

		// WRITEBACK
		// ---------

		// reg
		public int wb_wrEn;			// Register File Write Enable
		public int wb_wrAdd;
		public int wb_muxDS;
		public int wb_aluOut;
		public int wb_mD;

		// comb
		public int wb_data;

		// MEMORIES
		// --------
		public int im_dOut;
		public int rf_dOutA;
		public int rf_dOutB;
		public int dm_dOut;

		public Object clone()
		{
			try
			{
				return super.clone();
			}
			catch (CloneNotSupportedException e)
			{
				return null;
			}
		}
	}

	public class Controls
	{
		public int pcEn;
		public int feFlush;
		public int deFlush;
		public int fedeEn;
		public int deexEn;
		public int wrEn;
		public int memEn;
		public AluOp aluOp;

		public int muxFA;
		public int muxFB;
		public int muxAS;
		public int muxJS;
		public int muxDS;
		public int muxCS;
	}

	public Instruction[] program;
	public Signals signals;
	public Controls controls;


	public Processor(Instruction[] program)
	{
		this.program = program;
		reset();
	}
	
	public void reset(Instruction[] program) {
		this.program = program;
		reset();
	}

	public void reset()
	{
		signals = new Signals();
		controls = new Controls();

		registerFile = new int[16];
		dataMemory = new int[256];

		wbInsnAdd = 0;
		maInsnAdd = 0;
		exInsnAdd = 0;
		deInsnAdd = 0;
		feInsnAdd = 0;

		updateControls();
		updateCombinatorial();

		clockCount = 0;
	}

	public void clock()
	{
		clockReg();

		wbInsnAdd = maInsnAdd;
		maInsnAdd = exInsnAdd;
		if (controls.deexEn == 1) exInsnAdd = deInsnAdd;
		if (controls.fedeEn == 1) deInsnAdd = feInsnAdd;
		feInsnAdd = signals.fe_pc;

		updateControls();
		updateCombinatorial();

		clockCount ++;
	}

	private void clockReg()
	{
		Signals oldSignals = signals;
		signals = (Signals) oldSignals.clone();		

		// Fetch
		if (controls.pcEn == 1)
		{
			signals.fe_pc			= oldSignals.ex_nPC;
		}

		// Decode
		if (controls.fedeEn == 1)
		{
			signals.de_pc			= oldSignals.fe_pc;
			signals.de_feFlush	= controls.feFlush;

			// Instruction Memory Latch
			signals.de_insn = oldSignals.im_dOut;
		}

		// Execute
		if (controls.deexEn == 1)
		{
			signals.ex_pc			= oldSignals.de_pc;
			signals.ex_deFlush	= controls.deFlush;
			signals.ex_feFlush	= oldSignals.de_feFlush;
			signals.ex_opCode		= oldSignals.de_opCode;
			signals.ex_rAddA		= oldSignals.de_rAddA;
			signals.ex_rAddB		= oldSignals.de_rAddB;
			signals.ex_imm			= oldSignals.de_imm;

			// Register File Latch
			signals.ex_rDatA = oldSignals.rf_dOutA;
			signals.ex_rDatB = oldSignals.rf_dOutB;
		}

		// Memory Access
		signals.ma_wrEn		= controls.wrEn;
		signals.ma_wrAdd		= oldSignals.ex_rAddA;
		signals.ma_muxDS		= controls.muxDS;
		signals.ma_aluOut		= oldSignals.ex_aluOut;
		signals.ma_mAdd		= oldSignals.ex_mAdd;
		signals.ma_mEn			= controls.memEn;

		signals.ma_flagZ		= oldSignals.ex_flagZ;
		signals.ma_flagC		= oldSignals.ex_flagC;

		// Writeback
		signals.wb_wrEn		= oldSignals.ma_wrEn;
		signals.wb_wrAdd		= oldSignals.ma_wrAdd;
		signals.wb_muxDS		= oldSignals.ma_muxDS;
		signals.wb_aluOut		= oldSignals.ma_aluOut;
		signals.wb_mD			= oldSignals.dm_dOut;

		// Data Memory Write
		if (oldSignals.ma_mEn == 1) dataMemory[oldSignals.ma_mAdd] = oldSignals.ma_aluOut;

		// Register File Write
		if (oldSignals.wb_wrEn == 1) registerFile[oldSignals.wb_wrAdd] = oldSignals.wb_data;
	}

	private void updateControls()
	{
		// Defaults
		controls.aluOp = AluOp.OPA;

		controls.pcEn = 1;
		controls.fedeEn = 1;
		controls.deexEn = 1;
		controls.wrEn = 0;
		controls.memEn = 0;

		controls.muxCS = 0;
		controls.muxJS = 0;
		controls.muxFA = 0;
		controls.muxFB = 0;
		controls.muxAS = 0;
		controls.muxDS = 0;

		controls.feFlush = 0;
		controls.deFlush = 0;

		// If instruction has been flushed, use defaults
		if (signals.ex_deFlush == 1 || signals.ex_feFlush == 1) return;

		switch (Instruction.getType(signals.ex_opCode))
		{
			case ADDi :
				controls.aluOp = AluOp.ADD;
				controls.wrEn = 1;
				controls.muxFB = 1;
				break;
			case ADDr :
				controls.aluOp = AluOp.ADD;
				controls.wrEn = 1;
				break;

//			case ADDCi :
//				controls.wrEn = 1;
//				controls.muxFB = 1;
//				break;
//			case ADDCr :
//				controls.wrEn = 1;
//				break;

			case SUBi :
				controls.aluOp = AluOp.SUB;
				controls.wrEn = 1;
				controls.muxFB = 1;
				break;
			case SUBr :
				controls.aluOp = AluOp.SUB;
				controls.wrEn = 1;
				break;

//			case SUBCi :
//				controls.wrEn = 1;
//				controls.muxFB = 1;
//				break;
//			case SUBCr :
//				controls.wrEn = 1;
//				break;

			case COMPi :
				controls.aluOp = AluOp.SUB;
				controls.muxFB = 1;
				break;
			case COMPr :
				controls.aluOp = AluOp.SUB;
				break;

//			case NEGr :
//				controls.wrEn = 1;
//				break;

			case ANDi :
				controls.aluOp = AluOp.AND;
				controls.wrEn = 1;
				controls.muxFB = 1;
				break;
			case ANDr :
				controls.aluOp = AluOp.AND;
				controls.wrEn = 1;
				break;

			case ORi :
				controls.aluOp = AluOp.OR;
				controls.wrEn = 1;
				controls.muxFB = 1;
				break;
			case ORr :
				controls.aluOp = AluOp.OR;
				controls.wrEn = 1;
				break;

			case XORi :
				controls.aluOp = AluOp.XOR;
				controls.wrEn = 1;
				controls.muxFB = 1;
				break;
			case XORr :
				controls.aluOp = AluOp.XOR;
				controls.wrEn = 1;
				break;

//			case ROTi :
//				controls.wrEn = 1;
//				controls.muxFB = 1;
//				break;
//			case ROTr :
//				controls.wrEn = 1;
//				break;

			case SHLi :
				controls.aluOp = AluOp.SHL;
				controls.wrEn = 1;
				controls.muxFB = 1;
				break;
			case SHLr :
				controls.aluOp = AluOp.SHL;
				controls.wrEn = 1;
				break;

			case SHRi :
				controls.aluOp = AluOp.SHR;
				controls.wrEn = 1;
				controls.muxFB = 1;
				break;
			case SHRr :
				controls.aluOp = AluOp.SHR;
				controls.wrEn = 1;
				break;

			case MOVi :
				controls.aluOp = AluOp.OPA;
				controls.wrEn = 1;
				controls.muxFA = 1;
				break;
			case MOVr :
				controls.aluOp = AluOp.OPB;
				controls.wrEn = 1;
				break;

			// LOAD / STORE
			// ------------
			case LDi :
				controls.aluOp = AluOp.OPB;
				controls.muxAS = 1;
				controls.muxFB = 1;
				controls.wrEn = 1;
				controls.muxDS = 1;
				break;

			case LDr :
				controls.aluOp = AluOp.OPB;
				controls.muxAS = 1;
				controls.wrEn = 1;
				controls.muxDS = 1;
				break;


			case STi :
				controls.aluOp = AluOp.OPB;
				controls.muxFB = 1;
				controls.memEn = 1;
				break;

			case STa :
				controls.aluOp = AluOp.OPA;
				controls.muxFB = 1;
				controls.muxAS = 1;
				controls.memEn = 1;
				break;

			case STr :
				controls.aluOp = AluOp.OPB;
				controls.memEn = 1;
				break;

			// JUMPS / BRANCHES
			// ----------------
			case JMPi :
				controls.muxJS = 1;
				controls.muxCS = 2;
				controls.feFlush = 1;
				controls.deFlush = 1;
				break;

//			case JMPRi :
//				break;

//			case JMPRr :
//				break;


			case BRZi :
				if (signals.ma_flagZ == 1)
				{
					controls.muxJS = 1;
					controls.muxCS = 2;
					controls.feFlush = 1;
					controls.deFlush = 1;
				}
				break;

			case BRNZi :
				if (signals.ma_flagZ == 0)
				{
					controls.muxJS = 1;
					controls.muxCS = 2;
					controls.feFlush = 1;
					controls.deFlush = 1;
				}
				break;

			default:
		}

		// If Jump Flush is disabled, clear flush controls
		if (enableJumpFlush == false)
		{
			controls.feFlush = 0;
			controls.deFlush = 0;
		}

		// Forwarding
		if (enableRegForwarding && controls.muxFA == 0)
		{
			if (signals.ex_rAddA == signals.ma_wrAdd && signals.ma_wrEn == 1) controls.muxFA = 3;
			else if (signals.ex_rAddA == signals.wb_wrAdd && signals.wb_wrEn == 1) controls.muxFA = 2;
		}

		if (enableRegForwarding && controls.muxFB == 0)
		{
			if (signals.ex_rAddB == signals.ma_wrAdd && signals.ma_wrEn == 1) controls.muxFB = 3;
			else if (signals.ex_rAddB == signals.wb_wrAdd && signals.wb_wrEn == 1) controls.muxFB = 2;
		}

		// Load Stall
		if (enableLoadStall && signals.ma_muxDS == 1 && signals.ma_wrEn == 1)
		{
			controls.pcEn = 0;
			controls.fedeEn = 0;
			controls.deexEn = 0;
			controls.wrEn = 0;
		}
	}

	private void updateCombinatorial()
	{
		signals.fe_pcPlus = (signals.fe_pc + 1) & 0xFFF;

		signals.de_opCode		= (signals.de_insn >> 12) & 0x3F;
		signals.de_rAddA		= (signals.de_insn >> 8) & 0xF;
		signals.de_rAddB		= signals.de_insn & 0xF;
		signals.de_imm			= signals.de_insn & 0xFFF;

		// Mux DS
		switch (signals.wb_muxDS)
		{
			case 0 :	signals.wb_data = signals.wb_aluOut;	break;
			case 1 :	signals.wb_data = signals.wb_mD;			break;
			default : System.err.println("Processor.updateCombinatorial: Invalid controls.muxDS value (" + controls.muxDS + ")");
		}

		// Mux FA
		switch (controls.muxFA)
		{
			case 0 :	signals.ex_opA = signals.ex_rDatA;				break;
			case 1 :	signals.ex_opA = (signals.ex_imm & 0xFF);		break;
			case 2 :	signals.ex_opA = signals.wb_data;				break;
			case 3 :	signals.ex_opA = signals.ma_aluOut;				break;
			default : System.err.println("Processor.updateCombinatorial: Invalid controls.muxFA value (" + controls.muxFA + ")");
		}

		// Mux FB
		switch (controls.muxFB)
		{
			case 0 :	signals.ex_opB = signals.ex_rDatB;				break;
			case 1 :	signals.ex_opB = (signals.ex_imm & 0xFF);		break;
			case 2 :	signals.ex_opB = signals.wb_data;				break;
			case 3 :	signals.ex_opB = signals.ma_aluOut;				break;
			default : System.err.println("Processor.updateCombinatorial: Invalid controls.muxFB value (" + controls.muxFB + ")");
		}

		// Mux AS
		switch (controls.muxAS)
		{
			case 0 :	signals.ex_mAdd = signals.ex_opA; break;
			case 1 :	signals.ex_mAdd = signals.ex_opB; break;
			default : System.err.println("Processor.updateCombinatorial: Invalid controls.muxAS value (" + controls.muxAS + ")");
		}

		// Sign Extend
		signals.ex_opASE = signals.ex_opA;
		if ((signals.ex_opA & 0x80) == 0x80) signals.ex_opASE |= 0xF00;

		// PC Add
		signals.ex_pcRel = (signals.ex_opASE + signals.ex_pc) & 0xFFF;

		// Mux JS
		switch (controls.muxJS)
		{
			case 0 :	signals.ex_pcJmp = signals.ex_pcRel;	break;
			case 1 :	signals.ex_pcJmp = signals.ex_imm;		break;
			default : System.err.println("Processor.updateCombinatorial: Invalid controls.muxJS value (" + controls.muxJS + ")");
		}

		// ALU
		boolean setFlags = false;
		switch (controls.aluOp)
		{
			case ADD :	signals.ex_aluOut = signals.ex_opA + signals.ex_opB;
							setFlags = true; break;
//			case ADDC :	break;

			case SUB :	signals.ex_aluOut = signals.ex_opA - signals.ex_opB;
							setFlags = true; break;
//			case SUBC :	break;

			case AND :	signals.ex_aluOut = signals.ex_opA & signals.ex_opB;
							setFlags = true; break;
			case XOR :	signals.ex_aluOut = signals.ex_opA ^ signals.ex_opB;
							setFlags = true; break;
			case OR :	signals.ex_aluOut = signals.ex_opA | signals.ex_opB;
							setFlags = true; break;

			case OPA :	signals.ex_aluOut = signals.ex_opA; break;
			case OPB :	signals.ex_aluOut = signals.ex_opB; break;

//			case ROL :	break;

			case SHL :	signals.ex_aluOut = signals.ex_opA << signals.ex_opB;
							setFlags = true; break;
			case SHR :	signals.ex_aluOut = signals.ex_opA >> signals.ex_opB;
							setFlags = true; break;

			default : System.err.println("Processor.updateCombinatorial: Invalid controls.ex_AluOp value (" + controls.aluOp + ")");
		}

		// Flags
		if (setFlags)
		{
			signals.ex_flagZ = ((signals.ex_aluOut & 0xFF) == 0) ? 1 : 0;
			signals.ex_flagC = ((signals.ex_aluOut & 0x100) == 0x100) ? 1 : 0;
		}

		// Mask ALU Output
		signals.ex_aluOut &= 0xFF;

		// Mux CS
		switch (controls.muxCS)
		{
			case 0 :	signals.ex_nPC = signals.fe_pcPlus;	break;
//			case 1 :	signals.ex_nPC = ;		break;
			case 2 :	signals.ex_nPC = signals.ex_pcJmp;	break;
			default : System.err.println("Processor.updateCombinatorial: Invalid controls.muxCS value (" + controls.muxCS + ")");
		}

		// Instruction Memory (Internal Read)
		signals.im_dOut = (signals.fe_pc >= program.length) ? 0 : program[signals.fe_pc].encoding;

		// Data Memory (Internal Read)
		signals.dm_dOut = dataMemory[signals.ma_aluOut];

		// Register File
		if (signals.wb_wrAdd == signals.de_rAddA && signals.wb_wrEn == 1) signals.rf_dOutA = signals.wb_data;
		else signals.rf_dOutA = registerFile[signals.de_rAddA];

		if (signals.wb_wrAdd == signals.de_rAddB && signals.wb_wrEn == 1) signals.rf_dOutB = signals.wb_data;
		else signals.rf_dOutB = registerFile[signals.de_rAddB];
	}
}

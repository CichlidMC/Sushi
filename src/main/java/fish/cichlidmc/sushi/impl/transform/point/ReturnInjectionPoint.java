package fish.cichlidmc.sushi.impl.transform.point;

import fish.cichlidmc.sushi.api.transform.inject.InjectionPoint;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReturnInjectionPoint implements InjectionPoint {
	public static final ReturnInjectionPoint ALL = new ReturnInjectionPoint(-1);

	public static final MapCodec<ReturnInjectionPoint> CODEC = Codec.INT.xmap(
			ReturnInjectionPoint::new, point -> point.index
	).fieldOf("index");

	private final int index;

	public ReturnInjectionPoint(int index) {
		this.index = index;
	}

	@Override
	@Nullable
	public Collection<? extends AbstractInsnNode> find(InsnList instructions) {
		List<AbstractInsnNode> list = new ArrayList<>();

		int current = 0;
		for (AbstractInsnNode insn : instructions) {
			if (isReturn(insn.getOpcode())) {
				if (current == this.index || this.index == -1) {
					list.add(insn);
				}

				current++;
			}
		}

		return list;
	}

	@Override
	public String describe() {
		return this.index == -1 ? "all returns" : "return #" + this.index;
	}

	@Override
	public MapCodec<? extends InjectionPoint> codec() {
		return CODEC;
	}

	public static boolean isReturn(int opcode) {
		return opcode == Opcodes.RETURN || opcode == Opcodes.ARETURN || opcode == Opcodes.FRETURN
				|| opcode == Opcodes.DRETURN || opcode == Opcodes.IRETURN || opcode == Opcodes.LRETURN;
	}
}

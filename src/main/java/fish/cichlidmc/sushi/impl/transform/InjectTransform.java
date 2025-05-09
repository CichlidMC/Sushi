package fish.cichlidmc.sushi.impl.transform;

import fish.cichlidmc.sushi.api.transform.HookingTransform;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.transform.TransformType;
import fish.cichlidmc.sushi.api.transform.inject.Cancellation;
import fish.cichlidmc.sushi.api.transform.inject.InjectionPoint;
import fish.cichlidmc.sushi.api.util.method.MethodDescription;
import fish.cichlidmc.sushi.api.util.method.MethodTarget;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.util.Collection;

public final class InjectTransform extends HookingTransform {
	public static final MapCodec<InjectTransform> CODEC = CompositeCodec.of(
			MethodTarget.CODEC.fieldOf("method"), (InjectTransform inject) -> inject.method,
			MethodDescription.WITH_CLASS_CODEC.fieldOf("hook"), inject -> inject.hook,
			InjectionPoint.CODEC.fieldOf("point"), inject -> inject.point,
			InjectTransform::new
	);

	public static final TransformType TYPE = new TransformType(CODEC);

	private final InjectionPoint point;

	private InjectTransform(MethodTarget method, MethodDescription hook, InjectionPoint point) {
		super(method, hook);
		this.point = point;
	}

	@Override
	protected boolean apply(TransformContext context, MethodNode method, Method hook) throws TransformException {
		Collection<? extends AbstractInsnNode> targets = this.point.find(method.instructions);
		if (targets.isEmpty())
			return false;

		for (AbstractInsnNode target : targets) {
			InsnList injection = this.buildInjection(hook);
			if (this.point.shift() == InjectionPoint.Shift.BEFORE) {
				method.instructions.insertBefore(target, injection);
			} else {
				method.instructions.insert(target, injection);
			}
		}

		return true;
	}

	private InsnList buildInjection(Method hook) {
		InsnList list = new InsnList();

		// TODO: insert parameters
		String desc = Type.getMethodDescriptor(hook);
		String owner = Type.getInternalName(hook.getDeclaringClass());
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, hook.getName(), desc));

		if (hook.getReturnType() != Void.TYPE) {
			// TODO: use the cancellation
			list.add(new InsnNode(Opcodes.POP));
		}

		return list;
	}

	@Override
	protected Method getAndValidateHook() throws TransformException {
		Method hook = super.getAndValidateHook();

		Class<?> returnType = hook.getReturnType();
		if (returnType != Void.TYPE && returnType != Cancellation.class) {
			throw new TransformException("Hook method must either return void or Cancellation: " + this.hook);
		}

		return hook;
	}

	@Override
	public String describe() {
		return "Inject @ [" + this.point.describe() + "] in [" + this.method + "] calling [" + this.hook + ']';
	}

	@Override
	public TransformType type() {
		return TYPE;
	}
}

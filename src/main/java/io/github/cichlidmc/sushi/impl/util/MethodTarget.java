package io.github.cichlidmc.sushi.impl.util;

import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.Codecs;
import io.github.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A partial description of a method. Specifies a name, and optionally parameters.
 */
public final class MethodTarget {
	public static final Codec<MethodTarget> SIMPLE_CODEC = Codecs.STRING.xmap(MethodTarget::new, target -> target.name);
	@SuppressWarnings("RedundantTypeArguments") // why are you like this?
	public static final Codec<MethodTarget> FULL_CODEC = CompositeCodec.<MethodTarget, String, Optional<List<JavaType>>>of(
			Codecs.STRING.fieldOf("name"), target -> target.name,
			JavaType.CODEC.listOf().optional().fieldOf("parameters"), target -> target.parameters,
			MethodTarget::new
	).asCodec();
	public static final Codec<MethodTarget> CODEC = FULL_CODEC.withAlternative(SIMPLE_CODEC);

	public final String name;
	public final Optional<List<JavaType>> parameters;

	private MethodTarget(String name) {
		this(name, Optional.empty());
	}

	private MethodTarget(String name, Optional<List<JavaType>> parameters) {
		this.name = name;
		this.parameters = parameters;
	}

	public Stream<MethodNode> filter(List<MethodNode> methods) {
		return this.filter(methods.stream(), node -> node.name, node -> Type.getArgumentTypes(node.desc));
	}

	public Stream<Method> filter(Method[] methods) {
		return this.filter(Arrays.stream(methods), Method::getName, Type::getArgumentTypes);
	}

	private <T> Stream<T> filter(Stream<T> stream, Function<T, String> nameGetter, Function<T, Type[]> paramsGetter) {
		return stream.filter(method -> this.matches(method, nameGetter, paramsGetter));
	}

	private <T> boolean matches(T method, Function<T, String> nameGetter, Function<T, Type[]> paramsGetter) {
		if (!Objects.equals(this.name, nameGetter.apply(method)))
			return false;

		if (!this.parameters.isPresent())
			return true;

		List<JavaType> parameters = this.parameters.get();
		Type[] methodParams = paramsGetter.apply(method);

		if (methodParams.length != parameters.size())
			return false;

		for (int i = 0; i < parameters.size(); i++) {
			JavaType expected = parameters.get(i);
			Type actual = methodParams[i];
			if (!expected.matches(actual)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(this.name);
		builder.append('(');
		if (this.parameters.isPresent()) {
			List<JavaType> params = this.parameters.get();
			for (int i = 0; i < params.size(); i++) {
				builder.append(params.get(i));
				if (i + 1 < params.size()) {
					builder.append(", ");
				}
			}
		} else {
			builder.append("<parameters unspecified>");
		}
		builder.append(')');
		return builder.toString();
	}
}

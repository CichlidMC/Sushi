package io.github.cichlidmc.sushi.impl.point;

import io.github.cichlidmc.sushi.api.util.SimpleRegistry;
import io.github.cichlidmc.sushi.impl.SushiInternals;
import io.github.cichlidmc.sushi.impl.util.NameMapper;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.map.MapCodec;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Collection;
import java.util.function.Function;

/**
 * Defines locations inside a method body where an injection should take place.
 */
public interface InjectionPoint {
	SimpleRegistry<MapCodec<? extends InjectionPoint>> REGISTRY = SimpleRegistry.create(SushiInternals::boostrapInjectionPoints);
	Codec<InjectionPoint> CODEC = SushiInternals.make(() -> {
		NameMapper<InjectionPoint> specialCases = new NameMapper<>();
		specialCases.put("head", HeadInjectionPoint.INSTANCE);
		specialCases.put("tail", TailInjectionPoint.INSTANCE);
		specialCases.put("return", ReturnInjectionPoint.ALL);
		return REGISTRY.byIdCodec().dispatch(InjectionPoint::codec, Function.identity())
				.withAlternative(specialCases.codec);
	});

	/**
	 * Find all instructions to use as injection targets.
	 * An injection will be inserted right before each returned instruction.
	 */
	Collection<AbstractInsnNode> find(InsnList instructions);

	MapCodec<? extends InjectionPoint> codec();

}

package fish.cichlidmc.sushi.api.transform.expression;

import fish.cichlidmc.sushi.api.transform.Transform;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.util.SimpleRegistry;
import fish.cichlidmc.sushi.impl.SushiInternals;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.InsnList;

/**
 * Defines an expression in a method body that can be targeted for modification.
 */
public interface ExpressionTarget {
	SimpleRegistry<MapCodec<? extends ExpressionTarget>> REGISTRY = SimpleRegistry.create(SushiInternals::bootstrapExpressionTargets);
	Codec<ExpressionTarget> CODEC = Codec.codecDispatch(REGISTRY.byIdCodec(), ExpressionTarget::codec);

	/**
	 * Find all expressions matching this target.
	 * @throws TransformException if something goes wrong while finding targets
	 */
	@Nullable
	FoundExpressionTargets find(InsnList instructions) throws TransformException;

	/**
	 * @return a human-readable, single-line description of this target.
	 * <p>
	 * Examples: {@code all invokes of com.example.MyClass.myMethod}, {@code read #3 of com.example.MyClass.myField}
	 * @see Transform#describe()
	 */
	String describe();

	MapCodec<? extends ExpressionTarget> codec();
}

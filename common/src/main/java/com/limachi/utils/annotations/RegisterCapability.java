package com.limachi.utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * By using this annotation on a public static final field of type CapabilityToken{@code <your_data>},
 * the class declaring the token will be registered as a capability (or the declared class using the parameter 'cap' in the annotation),
 * with the name derived from the class name if not set.
 * <p>
 * <p>
 * If the parent class implements INBTSerializable, then the capability will be automatically stored on the capability holders.
 * <p>
 * <p>
 * If the parent class implements ICopyCapOnDeath and a holder of type Player is respawned due to a death, the capability will be persisted.
 * <p>
 * <p>
 * example:
 * <pre>
 * public class MyData implements {@code INBTSerializable<CompoundTag>}, {@code ICopyCapOnDeath<MyData>} {
 *
 *   {@code @}RegisterCapability(targets = {Entity.class})
 *   public static final {@code CapabilityToken<MyData>} TOKEN = new CapabilityToken<>(){};
 *
 *   int actual_data = 0;
 *   String another_data = "";
 *
 *   {@code @}Override
 *   public CompoundTag serializeNBT() {
 *       CompoundTag out = new CompoundTag();
 *       out.putInt("an_int", actual_data);
 *       out.putString("a_str", another_data);
 *       return out;
 *   }
 *
 *   {@code @}Override
 *   public void deserializeNBT(CompoundTag nbt) {
 *       actual_data = nbt.getInt("an_int");
 *       another_data = nbt.getString("a_str");
 *   }
 * }
 * </pre>
 * Will result in every entity (extending the class Entity) having the capability "my_data" attached and serialized, containing an int and a string.
 * To access the capability of an entity, you can simply use entity.getCapability(CapabilityManager.get(TOKEN))
 * <p>
 * <p>
 * I suggest using a static method to simplify the capability access like this:
 * <pre>
 *   public static boolean runOn(Entity entity, {@code NonNullConsumer<MyData>} consumer) {
 *       {@code LazyOptional<MyData>} laz = entity.getCapability(CapabilityManager.get(TOKEN));
 *       laz.ifPresent(consumer);
 *       return laz.isPresent();
 *   }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RegisterCapability {
    /**
     * list of classes to automatically attach the capability to (you can also manually attach capabilities),
     * valid classes are Entity, Level, ItemStack, BlockEntity, LevelChunk and any extensions of those 5 classes
     */
    Class<?>[] targets() default {};
    /**
     * serialized name of the capability, if unset, use the name of the parent class in snake case
     */
    String name() default "";

    /**
     * use this method path to make this registry optional (method should be static, of the format `boolean method(Class<?> annotation, String name)`, returning true if the object should be skipped, aka not registered)
     */
    String skip() default "";

    /**
     * instead of using the parent class as capability, use this class.
     * allow the token to be stored on the interface instead of on the implementation (better for exposing in an api).
     */
    Class<?> cap() default Void.class;
}

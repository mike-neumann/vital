package me.vitalframework

/**
 * Defines an entity managed by a [VitalRepository] instance.
 * The entity may be identified by the given generic type.
 *
 * ```java
 * public class MyEntity implements VitalEntity<UUID> {
 *   private UUID id;
 *
 *   public UUID getId() {
 *     return id;
 *   }
 *
 *   public void setId(UUID id) {
 *     this.id = id;
 *   }
 * }
 * ```
 */
interface VitalEntity<T> {
    var id: T
}

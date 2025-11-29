package me.vitalframework.players

import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service class responsible for managing `VitalPlayer` instances and their interactions
 * with the `VitalPlayerRepository`. Provides methods for creating, removing, and retrieving players.
 *
 * @param playerRepository The repository used for storing and managing `VitalPlayer` instances.
 */
@Service
class VitalPlayerService(
    private val playerRepository: VitalPlayerRepository,
) {
    /**
     * Creates and saves a new player instance in the repository.
     *
     * This method initializes a new `VitalPlayer` instance of the specified class and associates it with
     * the provided player and its unique identifier. If a player with the same unique identifier already exists
     * in the repository, an exception is thrown.
     *
     * @param T The type of the player being created.
     * @param player The player instance to be associated with the `VitalPlayer`.
     * @param playerUniqueId The unique identifier of the player.
     * @param playerClass The class type of the player instance being created.
     * @param vitalPlayerClass The class type of the `VitalPlayer` associated with the specified player.
     * @throws VitalPlayerException.AlreadyExists if a player with the given unique identifier already exists in the repository.
     * @throws VitalPlayerException.Create if there is an error during the creation of the `VitalPlayer` instance.
     */
    fun <T : Any> createPlayer(
        player: T,
        playerUniqueId: UUID,
        playerClass: Class<T>,
        vitalPlayerClass: Class<out VitalPlayer<*>>,
    ) = try {
        if (playerRepository.exists(playerUniqueId)) {
            throw VitalPlayerException.AlreadyExists(
                playerClass,
                playerUniqueId,
            )
        }
        playerRepository.save(
            vitalPlayerClass.getDeclaredConstructor(playerClass).newInstance(playerClass.cast(player)),
        )
    } catch (e: Exception) {
        throw VitalPlayerException.Create(vitalPlayerClass, playerUniqueId, e)
    }

    /**
     * Removes a player from the player repository using their unique identifier.
     *
     * If the player does not exist in the repository, the method returns without performing any action.
     *
     * @param playerUniqueId The unique identifier of the player to be removed.
     */
    fun destroyPlayer(playerUniqueId: UUID) {
        val vitalPlayer = playerRepository.get(playerUniqueId) ?: return
        playerRepository.delete(vitalPlayer)
    }

    /**
     * Retrieves a list of all `VitalPlayer` instances managed by the `playerRepository`.
     *
     * This method casts the stored entities in the `playerRepository` to a list of the specified type [T],
     * which must be a subclass of `VitalPlayer`.
     *
     * @param T The specific type of `VitalPlayer` to retrieve.
     * @return A list of players of type [T].
     * @throws ClassCastException If the entities in `playerRepository` cannot be cast to the specified type [T].
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : VitalPlayer<*>> getPlayers() = playerRepository.entities as List<T>

    /**
     * Retrieves a `VitalPlayer` instance of the specified type associated with the given unique identifier.
     *
     * @param id The unique identifier of the requested `VitalPlayer`.
     * @return The `VitalPlayer` instance of type `T` associated with the provided identifier, or `null` if no matching player is found.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : VitalPlayer<*>> getPlayer(id: UUID) = playerRepository.get(id) as T?
}

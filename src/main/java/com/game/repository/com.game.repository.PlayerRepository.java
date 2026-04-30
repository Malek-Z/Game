public interface PlayerRepository extends JpaRepository<Player, Long> {
    Player findByName(String name);
}
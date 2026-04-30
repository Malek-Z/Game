@Entity
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int highScore;
    private int progression; // Level reached
    // Getters and Setters
}
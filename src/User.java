public class User {
    private String username;
    private String password;
    private int quizPlayed;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getQuizPlayed() {
        return quizPlayed;
    }

    public void setQuizPlayed(int quizPlayed) {
        this.quizPlayed = quizPlayed;
    }

    public void incrementQuizPlayed(){
        this.quizPlayed++;
    }
}
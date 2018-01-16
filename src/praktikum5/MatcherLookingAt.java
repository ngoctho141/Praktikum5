package praktikum5;
 
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
public class MatcherLookingAt {
 
  public static void main(String[] args) {
      System.out.println("(?<declare>\\s*(int|float)\\s+[a-z]\\s*)=(?<value>\\s*\\d+\\s*);");
  }
}
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.IntStream;

public class Main {

    public static int getNumberOfMaxParam(int a, int b, int c) {
        int maxValue = IntStream.of(a, b, c).max().orElse(0);
        return Arrays.asList(a, b, c).indexOf(maxValue) + 1;
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        final int a = scanner.nextInt();
        final int b = scanner.nextInt();
        final int c = scanner.nextInt();

        System.out.println(getNumberOfMaxParam(a, b, c));
    }
}
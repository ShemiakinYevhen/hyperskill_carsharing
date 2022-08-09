import java.util.List;
import java.util.function.*;
import java.util.stream.*;

class FunctionUtils {

    public static <T> Supplier<Stream<T>> saveStream(Stream<T> stream){
        List<T> values = stream.collect(Collectors.toList());
        return values::stream;
    }

}
package net.servboot.orm;

import java.io.Serializable;
import java.util.function.Function;

public interface ServBootFunction <T, K> extends Function<T, K>, Serializable {
}

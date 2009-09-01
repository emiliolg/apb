package apb.utils;

import java.util.Collection;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Some utility Collection Methods
 */
public class CollectionUtils {
    /**
     * Ensures that this collection contains the specified element.
     * But only tries to add it if the element is not null
     * @param collection
     * @param element The elment to be added if not null
     * @param <T> The type of the element that must match the type of the collection
     */
    public static<T> void addIfNotNull(@NotNull Collection<T> collection, @Nullable T element) {
        if (element != null) {
            collection.add(element);
        }
    }

    /**
     * Creates a java.util.List with the specified (Optional) element
     * If the element is null it returns the empty List
     * @param element The element to create the List from
     * @param <T> The type of the element and the List
     * @return A singleton list if the element is not null an empty list otherwise.
     */
    public static<T> List<T> optionalSingleton(@Nullable T element) {
        if (element == null) return Collections.emptyList();
        else return Collections.singletonList(element);
    }

    public static void copyProperties(Map<String,String> m, Properties p)
    {
        for (String id: p.stringPropertyNames()) {
            m.put(id, p.getProperty(id));
        }
    }
}

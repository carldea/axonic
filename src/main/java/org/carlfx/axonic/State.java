package org.carlfx.axonic;

import java.util.List;

///
///```mermaid
///  graph TD;
///    A-->B;
///    A-->C;
///    B-->D;
///    C-->D;
///```
///
///
/// ```java
/// /** Hello World! */
/// public class HelloWorld {
///     public static void main(String... args) {
///         System.out.println("Hello World!"); // the traditional example
///     }
/// }
/// ```
///
///
public interface State {
    /**
     * State name
     * @return name of state
     */
    String getName();
}

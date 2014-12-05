## Nazgul Core: Persistence Component

The Nazgul Core: Persistence Component defines a simple way to combine JPA and JAXB to create
entities that can be persisted (typically to a database using JPA or SQL) and also marshalled
(typically to a web service using JAXB and XML/JSON). The two project within the component are

1. **persistence-model**: Contains entity specifications used to work with JPA and JAXB in a
   simple manner. Also integrates the Nazgul Tools: Validation API and Aspect to automatically
   call a validation method after construction (even if running Java SE or otherwise outside
   of a JPA/JavaEE container).
2. **persistence-api**: Contains convenience methods to simplify work with the Model classes.

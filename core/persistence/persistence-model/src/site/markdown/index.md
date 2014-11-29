## Nazgul Core: Persistence Model

The Nazgul Core: Persistence Model defines a simple way to combine JPA and JAXB to create
entities that can be persisted (typically to a database using JPA or SQL) and also marshalled
(typically to a web service using JAXB and XML/JSON).

#### Using NazgulEntity to work with Java Persistence API (JPA)-enabled applications

Using JPA to read or store entities as rows within (joined) database tables can be convenient
as frequently shown within Java and JEE tutorials. However, the JPA tutorials generally use
trivial examples that ignores required real-life application mechanics such as the effects of
not using a simple-to-index primary key type or mapping Entities to and from a database view.
The NazgulEntity serves as a superclass where some of the required mechanics of entities, such
as having a sensible choice of primary key or being Validatable, are provided.

The NazgulEntity (and simple decendent NazgulMudableIdEntity) serve as a point of origin
for anything requiring persistence, since the choices made in the Nazgul Core: Persistence Model
are sufficient to create a simple application, as well as scale to huge application volumes.
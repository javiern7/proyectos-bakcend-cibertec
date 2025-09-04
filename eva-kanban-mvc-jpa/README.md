# EVA Kanban MVC (Spring MVC + Thymeleaf)

Proyecto listo para Tomcat/Jetty (WAR) con:
- Tablero Kanban drag & drop (ASSIGNED, IN_PROGRESS, DONE)
- Roles: ADMIN (ve todo, crea y reasigna), USER (ve solo sus tareas, cambia estado de las suyas)
- Sesión (`@SessionAttributes("user")`), `@ModelAttribute` (estados y métricas)
- Aspecto AOP de bitácora (creación y actualización)
- Configuración por XML (`web.xml` + `applicationcontext-servlet.xml`)
- Templates Thymeleaf

## Requisitos
- JDK 11+ (recomendado JDK 17)
- Maven 3.8+
- Tomcat 9+

## Build
```bash
mvn clean package
```
Generará `target/eva-kanban-mvc-jpa.war`

## Deploy
Copia el WAR a `TOMCAT_HOME/webapps/` (o configura un Run Configuration en IntelliJ).

## URLs
- `http://localhost:8080/eva-kanban-mvc-jpa/login.cibertec`
- Luego del login: `http://localhost:8080/eva-kanban-mvc-jpa/board.cibertec`

## Usuarios demo
- admin / admin  (ADMIN)
- jnavarro / 123 (USER)

## Notas
- Logging con Logback (ver consola del servidor).
- Puedes crear tareas y arrastrarlas entre columnas. ADMIN puede reasignar.

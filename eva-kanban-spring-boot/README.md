BOOTIFY de tu proyecto Kanban

Estructura esperada en Spring Boot:
- src/main/resources/templates      <-- MUEVE aquí tus vistas Thymeleaf (board.html, fragments, etc.)
- src/main/resources/static/js      <-- MUEVE aquí tus JS (app.js, dashboard.js)
- src/main/resources/static/css     <-- y CSS, imágenes, etc.

Pasos de migración:
1) Copia TODAS las clases Java (entities, repos, services, controllers) de tu proyecto actual a
   src/main/java/pe/edu/cibertec/eva  (mantén paquetes).
   Boot escanea automáticamente porque la clase KanbanApplication está en 'pe.edu.cibertec.eva'.

2) Mueve vistas y estáticos:
    - /src/main/webapp/WEB-INF/templates/**  -->  /src/main/resources/templates/**
    - /src/main/webapp/js/**                 -->  /src/main/resources/static/js/**
    - Ajusta en board.html que las rutas sigan siendo th:src="@{/js/...}" (ya lo usas).

3) Context path:
    - Ya quedó en application.properties: server.servlet.context-path=/eva_kanban_mvc_jpa
    - Tu variable window.ctx en layout.html continuará funcionando.

4) Dependencias:
    - El pom.xml ya incluye web, thymeleaf, data-jpa, validation, MySQL, Apache POI.
    - Si usas otra BD, cambia el driver y dialecto.

5) Ejecutar:
   mvn -q -DskipTests package
   java -jar target/eva-kanban-boot-1.0.0.jar
   # o en desarrollo:
   mvn spring-boot:run

6) Notas:
    - Si NO usas Spring Security, no agregues el starter de security.
    - Si tus @Repositories no están bajo 'pe.edu.cibertec.eva', mueve paquetes o añade @EnableJpaRepositories.
    - Revisa campos de fecha: en Auditoría usa 'createdAt' (no 'when').
    - dashboard.js: si se ve alto, mantén la línea: chartCanvas.style.height='260px'.

## Execution Service

El Execution Service es el módulo encargado de ejecutar snippets PrintScript y validar tests asociados.
Se integra con:

Snippet Service → obtiene los snippets a ejecutar  
Test Service → obtiene los casos de test asociados a un snippet  
PrintScript Core → lexer, parser, validator, interpreter

Su responsabilidad principal es ejecutar código PrintScript de manera programática, controlar inputs/envs, capturar errores y comparar outputs para tests.

---------------------------------------
Arquitectura General
---------------------------------------
execution

├── common/…

├── execution

│     ├── ExecutionController.kt

│     ├── ExecutionService.kt

│     ├── dto/

│     └── util/

├── runner/

│     ├── ListInputReader.kt

│     ├── ListEnvReader.kt

│     ├── ExecutionOutputWriter.kt

│     └── InMemoryDiagnosticReporter.kt

├── snippet/

│     ├── SnippetClient.kt

│     ├── DefaultSnippetClient.kt

│     ├── dto/

│     └── SnippetServiceConfig.kt

└── testclient/

├── TestClient.kt

├── DefaultTestClient.kt

└── TestServiceConfig.kt

---------------------------------------
Módulo execution
---------------------------------------

### ExecutionController.kt

Expone los endpoints REST:

Método | Endpoint                                  | Descripción  
-------|-------------------------------------------|-------------  
POST | /api/execution/{snippetId}                | Ejecuta un snippet  
POST | /api/execution/{snippetId}/tests/{testId} | Ejecuta un test            

Ambos delegan en ExecutionService.

### ExecutionService.kt

Responsabilidades principales:

- Obtener el snippet mediante SnippetClient
- Obtener tests mediante TestClient (para test execution)
- Configurar adaptadores PrintScript:
    - ListInputReader para inputs
    - ListEnvReader para variables de entorno
    - ExecutionOutputWriter para capturar el output
    - InMemoryDiagnosticReporter para capturar errores
- Ejecutar el intérprete usando InterpreterRunner
- Crear el DTO final:
    - ExecutionResponseDTO para ejecuciones normales
    - TestExecutionResponseDTO para tests
- Validar el output en modo test

Flujo de ejecución:

snippet → readers → lexer → parser → validator → interpreter → outputs/reporter

---------------------------------------
2. Módulo runner
---------------------------------------

Contiene los adaptadores entre PrintScript y el Execution Service.

### ListInputReader
- Implementa InputReader.
- Recibe una colección de inputs.
- Devuelve uno por llamada en formato Sequence<Char>.
- Si no quedan inputs, devuelve vacío.
- Evita fallas por null.

### ListEnvReader
- Implementa EnvReader.
- Recibe un mapa de variables de entorno.
- Devuelve valores parseados (boolean, number, string).

### ExecutionOutputWriter
- Implementa OutputWriter.
- Captura todas las líneas impresas por PrintScript.
- Expone getAll() para obtenerlas.

### InMemoryDiagnosticReporter
- Implementa DiagnosticReporter.
- Guarda todos los errores y warnings.
- Permite que ExecutionService devuelva "ERROR" o "FAILED".

---------------------------------------
3. Integraciones externas
---------------------------------------

### SnippetClient

Provee:

getSnippetById(UUID): SnippetResponseDTO

Formato esperado:

```
SnippetResponseDTO(
 id: UUID,
 name: String,
 description: String,
 language: String,
 version: String,
 content: String,
 ownerId: String
)
```

### TestClient

Permite obtener tests del snippet.

getTestsForSnippet(snippetId: UUID): Collection<TestResponseDTO>

Formato TestResponseDTO:

```
TestResponseDTO(
 id: UUID,
 name: String,
 snippetId: UUID,
 inputs: Collection<String>,
 outputs: Collection<String>
)
```

---------------------------------------
4. DTOs del Execution Service
---------------------------------------

### ExecutionResponseDTO
- status: String  (OK | ERROR)
- output: String
- runtimeMs: Long

### TestExecutionResponseDTO
- status: String (PASSED | FAILED)
- runtimeMs: Long

---------------------------------------
5. Flujo completo – Ejecución de Snippet
---------------------------------------

Controller  
↓  
ExecutionService.executeSnippet()  
↓  
SnippetService → obtener snippet  
↓  
Configurar readers/writers  
↓  
InterpreterRunner.run()  
↓  
Si hay errores → reporter  
Si no → outputWriter  
↓  
Construcción de DTO

---------------------------------------
6. Flujo completo – Ejecución de Test
---------------------------------------

Controller  
↓  
ExecutionService.executeTest()  
↓  
SnippetService → snippet  
TestService → test  
↓  
Cargar inputs del test  
↓  
InterpreterRunner.run()  
↓  
Si hay errores → FAILED  
↓  
Comparar output real con outputs esperados  
↓  
PASSED | FAILED

---------------------------------------
7. Configuración
---------------------------------------

En `application.yml`:

```
services:
  snippet-service:
    url: http://snippet-service:8080
```

El mismo patrón aplica para `test-service`.

---------------------------------------
8. Consideraciones
---------------------------------------

- Ningún código externo se ejecuta fuera del intérprete PrintScript.
- Los readers y reporters están diseñados para evitar nulls y excepciones.
- El servicio funciona como un sandbox seguro para ejecutar PrintScript.

---------------------------------------
9. Endpoints finales
---------------------------------------

### Ejecutar un snippet
POST /api/execution/{snippetId}

### Ejecutar un test
POST /api/execution/{snippetId}/tests/{testId}

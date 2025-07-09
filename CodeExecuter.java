import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class CodeExecutor {

    public static String compileAndRun(String userCode) throws Exception {
        String className = "DynamicClass";
        String methodName = "run";

        // Prepare the complete Java code
        String fullCode = "public class " + className + " {\n" +
                          "    public static void " + methodName + "() {\n" +
                          "        " + userCode + "\n" +
                          "    }\n" +
                          "}";

        // Save to .java file
        File javaFile = new File(className + ".java");
        try (FileWriter writer = new FileWriter(javaFile)) {
            writer.write(fullCode);
        }

        // Compile
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int result = compiler.run(null, null, null, javaFile.getPath());
        if (result != 0) {
            return "Compilation Failed!";
        }

        // Capture output
        ByteArrayOutputStream outputCapture = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputCapture)); // Redirect output

        // Run the class
        try (URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{new File("").toURI().toURL()})) {
            Class<?> dynamicClass = Class.forName(className, true, classLoader);
            Method method = dynamicClass.getDeclaredMethod(methodName);
            method.invoke(null);
        }

        // Restore and return
        System.setOut(originalOut);
        javaFile.delete();
        new File(className + ".class").delete();

        String output = outputCapture.toString();
        System.out.print(output); // Print to terminal
        return output;           // Return for UI
    }
}
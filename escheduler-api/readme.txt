

maven 打包

mvn -U clean package assembly:assembly -Dmaven.test.skip=true

note : 如果启动springboot发生如下的错误
Unable to start embedded container; nested exception is java.lang.NoSuchMethodError: javax.servlet.ServletContext.addServlet(Ljava/lang/String;Ljavax/servlet/Servlet;)Ljavax/servlet/ServletRegistration$Dynamic;

solve method :

File -> Project Structure -> escheduler-server -> Dependencies remove servlet-api 2.5


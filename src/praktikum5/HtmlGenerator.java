package praktikum5;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
 
@Retention(RetentionPolicy.RUNTIME)
 
// Annotation này nói rằng:
// AnnHtmlUL chỉ được sử dụng cho Class, interface, annotation, enum.
@Target(value = { ElementType.TYPE })
 
// AnnHtmlUL: Mô phỏng thẻ (tag) <UL> trong HTML.
@interface AnnHtmlUL {
 
    public String border() default "border:1px solid blue;";
     
}

@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD, ElementType.METHOD })
 
// Mô phỏng thẻ (tag) <LI> trong HTML.
@interface AnnHtmlLI {
 
    public String background();
 
    public String color() default "red";
     
}

@AnnHtmlUL(border = "1px solid red")
class DocumentClass {
 
    private String author;
 
    @AnnHtmlLI(background = "blue", color = "black")
    public String getDocumentName() {
        return "Java Core";
    }
 
    @AnnHtmlLI(background = "yellow")
    public String getDocumentVersion() {
        return "1.0";
    }
 
    @AnnHtmlLI(background = "green")
    public void setAuthor(String author) {
        this.author = author;
    }
 
    @AnnHtmlLI(background = "red", color = "black")
    public String getAuthor() {
        return author;
    }
     
    // Phương thức này không được chú thích bởi Annotation nào.
    public float getPrice()  {
        return 100;
    }
 
}

public class HtmlGenerator {
 
    public static void main(String[] args) {
 
        Class<?> clazz = DocumentClass.class;
 
        // Kiểm tra xem lớp này có được chú thích (annotate) bởi AnnHtmlUL hay không.
        boolean isHtmlUL = clazz.isAnnotationPresent(AnnHtmlUL.class);
 
        StringBuilder sb = new StringBuilder();
        if (isHtmlUL) {
 
            // Lấy ra đối tượng AnnHtmlUL, chú thích trên lớp này.
            AnnHtmlUL annUL = clazz.getAnnotation(AnnHtmlUL.class);
 
            sb.append("<H3>" + clazz.getName() + "</H3>");
            sb.append("\n");
 
            // Lấy ra giá trị của phần tử 'border' của AnnHtmlUL.
            String border = annUL.border();
 
            sb.append("<UL style='border:" + border + "'>");
 
            // Thêm dấu xuống dòng.
            sb.append("\n");
 
            Method[] methods = clazz.getMethods();
 
            for (Method method : methods) {
                // Kiểm tra xem phương thức này có được chú thích (annotate)
                // bởi AnnHtmlLI hay không?
                if (method.isAnnotationPresent(AnnHtmlLI.class)) {
                    // Lấy ra annotation đó.
                    AnnHtmlLI annLI = method.getAnnotation(AnnHtmlLI.class);
 
                    // Lấy ra các giá trị các phần tử của AnnHtmlLI.
                    String background = annLI.background();
                    String color = annLI.color();
 
                    sb.append("<LI style='margin:5px;padding:5px;background:" + background + ";color:" + color + "'>");
                    sb.append("\n");
                    sb.append(method.getName());
                    sb.append("\n");
                    sb.append("</LI>");
                    sb.append("\n");
                }
            }
            sb.append("</UL>");
        }
        writeToFile(clazz.getSimpleName() + ".html", sb);
    }
 
    // Ghi các thông tin ra màn hình Console (Hoặc file).
    private static void writeToFile(String fileName, StringBuilder sb) {
        System.out.println(sb);
    }
 
}
 

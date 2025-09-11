package pe.edu.cibertec.eva.util;

public class Constants {

    private Constants() {
        throw new IllegalStateException();
    }

    public static final String ATRIBUT_TITLE = "pageTitle";
    public static final String ATRIBUT_CREATED_AT = "createdAt";
    public static final String ATRIBUT_ADMIN = "ADMIN";
    public static final String ATRIBUT_ROL_ADMIN = "ROLE_ADMIN";
    public static final String REDIRECT_ACCESS_DENIED = "redirect:/access-denied";

    //ASPECT ORDER
    public static final int SECURITY = 0;
    public static final int VALIDATION = 50;
    public static final int METRICS = 100;
    public static final int LOGGING = 200;

}

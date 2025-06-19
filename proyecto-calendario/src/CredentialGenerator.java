import java.security.SecureRandom;

/**
 * Interface para generación de credenciales
 * Define los contratos para generar nombres de usuario y contraseñas
 */
interface CredentialGenerator {
    /**
     * Genera un nombre de usuario único
     * @return nuevo nombre de usuario
     */
    String generateUsername();
    
    /**
     * Genera una contraseña segura
     * @return nueva contraseña generada
     */
    String generatePassword();
}

/**
 * Implementación que genera credenciales de forma secuencial
 * Los nombres de usuario siguen el patrón user001, user002, etc.
 * Las contraseñas son aleatorias de 8 caracteres alfanuméricos
 */
class SequentialCredentialGenerator implements CredentialGenerator {
    private int userCounter;
    private final SecureRandom random = new SecureRandom();
    
    // Caracteres permitidos para las contraseñas generadas
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int DEFAULT_PASSWORD_LENGTH = 8;

    /**
     * Constructor que inicializa el contador de usuarios
     * @param initialCounter valor inicial del contador (normalmente 1)
     */
    public SequentialCredentialGenerator(int initialCounter) {
        this.userCounter = initialCounter;
    }

    /**
     * Genera un nombre de usuario secuencial con formato user###
     * @return nombre de usuario único (ej: user001, user002, etc.)
     */
    @Override
    public String generateUsername() {
        return String.format("user%03d", userCounter++);
    }

    /**
     * Genera una contraseña aleatoria de 8 caracteres alfanuméricos
     * @return contraseña aleatoria segura
     */
    @Override
    public String generatePassword() {
        return generatePassword(DEFAULT_PASSWORD_LENGTH);
    }
    
    /**
     * Genera una contraseña aleatoria de longitud especificada
     * @param length longitud de la contraseña a generar
     * @return contraseña aleatoria de la longitud especificada
     */
    public String generatePassword(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Password length must be positive");
        }
        
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * Obtiene el valor actual del contador de usuarios
     * @return contador actual (próximo número a usar)
     */
    public int getUserCounter() {
        return userCounter;
    }
    
    /**
     * Establece el valor del contador de usuarios
     * @param userCounter nuevo valor del contador
     */
    public void setUserCounter(int userCounter) {
        if (userCounter < 1) {
            throw new IllegalArgumentException("User counter must be at least 1");
        }
        this.userCounter = userCounter;
    }
    
    /**
     * Genera un nombre de usuario con un prefijo personalizado
     * @param prefix prefijo para el nombre de usuario
     * @return nombre de usuario con el prefijo especificado
     */
    public String generateUsernameWithPrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return generateUsername();
        }
        return String.format("%s%03d", prefix.trim(), userCounter++);
    }
    
    /**
     * Genera una contraseña con criterios específicos
     * @param includeUppercase incluir letras mayúsculas
     * @param includeLowercase incluir letras minúsculas
     * @param includeNumbers incluir números
     * @param includeSpecialChars incluir caracteres especiales
     * @param length longitud de la contraseña
     * @return contraseña generada según los criterios
     */
    public String generatePasswordWithCriteria(boolean includeUppercase, 
                                              boolean includeLowercase, 
                                              boolean includeNumbers, 
                                              boolean includeSpecialChars, 
                                              int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Password length must be positive");
        }
        
        StringBuilder charSet = new StringBuilder();
        
        if (includeUppercase) {
            charSet.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        }
        if (includeLowercase) {
            charSet.append("abcdefghijklmnopqrstuvwxyz");
        }
        if (includeNumbers) {
            charSet.append("0123456789");
        }
        if (includeSpecialChars) {
            charSet.append("!@#$%^&*()_+-=[]{}|;:,.<>?");
        }
        
        if (charSet.length() == 0) {
            throw new IllegalArgumentException("At least one character type must be included");
        }
        
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(charSet.charAt(random.nextInt(charSet.length())));
        }
        
        return password.toString();
    }
    
    /**
     * Genera una contraseña que garantiza incluir al menos un carácter de cada tipo solicitado
     * @param includeUppercase incluir al menos una mayúscula
     * @param includeLowercase incluir al menos una minúscula  
     * @param includeNumbers incluir al menos un número
     * @param length longitud total de la contraseña
     * @return contraseña que cumple los requisitos mínimos
     */
    public String generateStrongPassword(boolean includeUppercase, 
                                       boolean includeLowercase, 
                                       boolean includeNumbers, 
                                       int length) {
        if (length < 4) {
            throw new IllegalArgumentException("Strong password must be at least 4 characters long");
        }
        
        StringBuilder password = new StringBuilder();
        StringBuilder charSet = new StringBuilder();
        
        // Agregar al menos un carácter de cada tipo requerido
        if (includeUppercase) {
            String upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            password.append(upperChars.charAt(random.nextInt(upperChars.length())));
            charSet.append(upperChars);
        }
        
        if (includeLowercase) {
            String lowerChars = "abcdefghijklmnopqrstuvwxyz";
            password.append(lowerChars.charAt(random.nextInt(lowerChars.length())));
            charSet.append(lowerChars);
        }
        
        if (includeNumbers) {
            String numberChars = "0123456789";
            password.append(numberChars.charAt(random.nextInt(numberChars.length())));
            charSet.append(numberChars);
        }
        
        // Completar el resto de la longitud con caracteres aleatorios
        while (password.length() < length) {
            password.append(charSet.charAt(random.nextInt(charSet.length())));
        }
        
        // Mezclar los caracteres para que los obligatorios no estén siempre al inicio
        return shuffleString(password.toString());
    }
    
    /**
     * Mezcla aleatoriamente los caracteres de una cadena
     * @param input cadena a mezclar
     * @return cadena con caracteres en orden aleatorio
     */
    private String shuffleString(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
    
    /**
     * Resetea el contador a su valor inicial
     */
    public void resetCounter() {
        this.userCounter = 1;
    }
    
    /**
     * Obtiene información sobre el estado del generador
     * @return información del generador
     */
    public String getGeneratorInfo() {
        return String.format("SequentialCredentialGenerator - Next user number: %d", userCounter);
    }
}

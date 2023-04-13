import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class Main {
    static Map<Character, Integer> alphabet = new HashMap<>();

    /**
     * Выводит на экран меню для выбора действия, идет цикл пока не
     * будет выбрано корректное действие, вызывает нужный метод и завершается
     */

    public static void main(String[] args) {
        initAlphabet(alphabet);

        Scanner sc = new Scanner(System.in);

        int act;
        do {
            printMenu();
            act = Integer.parseInt(sc.nextLine());
            switch (act) {
                case 1:
                    executeEncryptWithKey(sc);
                    return;
                case 2:
                    executeBruteForce(sc);
                    return;
                case 0:
                    return;
                default:
                    System.out.println("Введите правильное число");
            }
        } while (true);
    }

    /**
     * Заполняет Map символами и их номерами, чтобы потом сдвигать их
     *
     * @param alphabet – пустая Map
     */
    private static void initAlphabet(Map<Character, Integer> alphabet) {
        String str = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя.,\":!? ";
        for (int i = 0; i < str.length(); i++) {
            alphabet.put(str.charAt(i), i);
        }
    }

    /**
     * Выводит меню на экран
     */
    private static void printMenu() {
        System.out.println("Выберите действие:\n" +
                "1) Зашифровать/расшифровать текст со смещением N\n" +
                "2) Расшифровать текст методом brute force (лучше текст побольше)\n" +
                "0) Выйти из программы");
    }

    /**
     * Запрашивает путь к файлу и смещение.
     * Создает новый файл в этой же папке с зашифрованным текстом.
     *
     * @param sc –сканнер
     */
    private static void executeEncryptWithKey(Scanner sc) {
        System.out.println("Введите путь (у меня src/text.txt): "); // у меня src/text.txt
        Path path = Path.of(sc.nextLine());
        System.out.println("Введите смещение N (если влево, введите отрицательное число): ");
        int N = Integer.parseInt(sc.nextLine());

        List<String> lines, outLines = new ArrayList<>();
        lines = readFile(path);

        encryptLines(N, lines, outLines);

        createNewPathAndFile(path, outLines);
    }

    /**
     * Читает файл и возвращает список строк
     *
     * @param path – путь
     * @return – список прочитанных строк
     */
    private static List<String> readFile(Path path) {
        List<String> lines;
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lines;
    }

    /**
     * шифрует текст
     *
     * @param N        – смещение (положительное или отрицательное)
     * @param lines    – исходный список строк
     * @param outLines – за/расшифрованный список строк
     */
    private static void encryptLines(int N, List<String> lines, List<String> outLines) {
        for (String text : lines) {
            StringBuilder outText = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    outText.append(c);
                } else {
                    Integer targetPos = defineTargetPos(N, alphabet.get(c));
                    if (targetPos == null) continue;
                    outText.append(matchCharToNewValue(targetPos));
                }
            }
            outLines.add(outText.toString());
        }
    }

    /**
     * определяет номер в алфавите нового символа
     *
     * @param N          – смещение
     * @param currentPos – номер текущего символа в алфавите
     * @return – номер нового символа
     */
    private static Integer defineTargetPos(int N, Integer currentPos) {
        if (currentPos == null)
            return null;
        int sum = currentPos + N;
        if (sum < 0)
            while (sum < 0)
                sum += 73;
        int targetPos = sum > 72 ? sum % 73 : sum;
        return targetPos;
    }

    /**
     * Возвращает char по номеру
     *
     * @param targetPos – позиция
     * @return char из нового текста
     */
    private static Character matchCharToNewValue(int targetPos) {
        for (Map.Entry<Character, Integer> entry : alphabet.entrySet()) {
            if (entry.getValue() == targetPos)
                return entry.getKey();
        }
        return null;
    }

    /**
     * Определяет имя, путь нового файла, и вызывает метод создания файла
     *
     * @param path     – путь изначального файла
     * @param outLines – новое содержание
     */
    public static void createNewPathAndFile(Path path, List<String> outLines) {
        Path newPath = createPath(path);

        createAndWriteFile(outLines, newPath);
    }

    /**
     * @param path – путь текущего файла
     * @return –путь к новому файлу
     */
    private static Path createPath(Path path) {
        String name = path.getFileName().toString();
        String nameWithoutExtension = name.substring(0, name.indexOf('.'));
        String extension = name.substring(name.indexOf('.'));
        int i = 1;
        while (Files.exists(Path.of(path.getParent() + "/" + nameWithoutExtension + i + extension))) {
            i++;
        }
        Path newPath = Path.of(path.getParent() + "/" + nameWithoutExtension + i + extension);
        return newPath;
    }

    /**
     * Создает новый файл и записывает его
     *
     * @param outLines – содержание
     * @param newPath  – новый путь файла
     */
    private static void createAndWriteFile(List<String> outLines, Path newPath) {
        try {
            Files.createFile(newPath);
            Files.write(newPath, outLines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Файл " + newPath + " создан!");
    }

    /**
     * Читает путь, сожердание файла. Вызывает методы расшифровки и записи в новый
     *
     * @param sc – сканнер
     */
    public static void executeBruteForce(Scanner sc) {
        System.out.println("Введите путь: (у меня src/text.txt)");
        Path path = Path.of(sc.nextLine());

        List<String> lines, outLines = new ArrayList<>();
        lines = readFile(path);

        decryptBruteForce(lines, outLines);

        createNewPathAndFile(path, outLines);
    }

    /**
     * Анализирует текст, текст с наибольшим количеством подходящих условий считает подходящим
     *
     * @param lines    – исходный список
     * @param outLines – итоговый список
     */
    private static void decryptBruteForce(List<String> lines, List<String> outLines) {
        TreeMap<Integer, Integer> allVars = new TreeMap<>(Collections.reverseOrder());
        for (int i = 0; i < 73; i++) {
            encryptLines(i, lines, outLines);
            int k = 0;
            for (String line : outLines) {
                if (line.length() == 0) {
                    continue;
                }
                for (int j = 1; j < line.length() - 1; j++) {
                    char prev = line.charAt(j - 1);
                    char current = line.charAt(j);
                    char next = line.charAt(j + 1);
                    if ((prev == '.' || prev == '!' || prev == '?') &&
                            current == ' ' && Character.toLowerCase(next) != next)
                        k++;
                    if (current == ',' && next == ' ')
                        k++;
                }
                char last = line.charAt(line.length() - 1);
                if (last == '.' || last == '!' || last == '?')
                    k++;
                char first = line.charAt(0);
                if (first != Character.toLowerCase(first))
                    k++;
            }
            outLines.clear();
            allVars.put(k, i);
        }

        int i = allVars.get(allVars.firstKey());
        encryptLines(i, lines, outLines);
    }
}
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class PaperCheck {
    public static void main(String[] args) {
        // ========== 单元自测（作业用，不需要JUnit） ==========
        selfTest();
        System.out.println("================================");

        // 原有查重业务逻辑
        if (args.length != 3) {
            System.err.println("参数错误！需要传入3个参数：orig.txt copy.txt result.txt");
            return;
        }
        String origPath = args[0];
        String copyPath = args[1];
        String outPath = args[2];
        try {
            long start = System.currentTimeMillis(); // 计时起点

            String origText = readFile(origPath);
            String copyText = readFile(copyPath);

            Set<String> origGram = get3GramSet(origText);
            Set<String> copyGram = get3GramSet(copyText);

            double rate = calcJaccard(origGram, copyGram);

            writeResult(outPath, rate);

            long end = System.currentTimeMillis(); // 计时终点

            System.out.println("查重完成，相似度：" + String.format("%.2f%%", rate * 100));
            System.out.println("程序总耗时：" + (end - start) + " 毫秒");

        } catch (FileNotFoundException e) {
            System.err.println("错误：找不到文件 -> " + e.getMessage());
        } catch (IOException e) {
            System.err.println("文件读写异常：" + e.getMessage());
        } catch (Exception e) {
            System.err.println("程序异常：" + e.getMessage());
        }
    }

    // 自测函数，替代JUnit单元测试
    public static void selfTest() {
        System.out.println("===== 开始单元自测 =====");
        int pass = 0;
        int fail = 0;

        // 测试1：正常文本生成3gram
        String t1 = "abcde";
        Set<String> g1 = get3GramSet(t1);
        if(g1.size()==3 && g1.contains("abc") && g1.contains("bcd") && g1.contains("cde")){
            System.out.println("测试1 通过");
            pass++;
        }else{
            System.out.println("测试1 失败");
            fail++;
        }

        // 测试2：文本不足3字符
        String t2 = "ab";
        Set<String> g2 = get3GramSet(t2);
        if(g2.isEmpty()){
            System.out.println("测试2 通过");
            pass++;
        }else{
            System.out.println("测试2 失败");
            fail++;
        }

        // 测试3：带空格文本，清除空白
        String t3 = "a b c";
        Set<String> g3 = get3GramSet(t3);
        if(g3.contains("abc")){
            System.out.println("测试3 通过");
            pass++;
        }else{
            System.out.println("测试3 失败");
            fail++;
        }

        // 测试4：两个集合完全相同
        Set<String> s4a = Set.of("aaa","bbb");
        Set<String> s4b = Set.of("aaa","bbb");
        if(Math.abs(calcJaccard(s4a,s4b)-1.0) < 0.001){
            System.out.println("测试4 通过");
            pass++;
        }else{
            System.out.println("测试4 失败");
            fail++;
        }

        // 测试5：无交集
        Set<String> s5a = Set.of("aaa");
        Set<String> s5b = Set.of("bbb");
        if(Math.abs(calcJaccard(s5a,s5b)-0.0) <0.001){
            System.out.println("测试5 通过");
            pass++;
        }else{
            System.out.println("测试5 失败");
            fail++;
        }

        // 测试6：部分相交
        Set<String> s6a = Set.of("aaa","bbb");
        Set<String> s6b = Set.of("bbb","ccc");
        if(Math.abs(calcJaccard(s6a,s6b)-1.0/3) <0.001){
            System.out.println("测试6 通过");
            pass++;
        }else{
            System.out.println("测试6 失败");
            fail++;
        }

        // 测试7：两个空集合
        Set<String> s7a = Set.of();
        Set<String> s7b = Set.of();
        if(Math.abs(calcJaccard(s7a,s7b)-1.0) <0.001){
            System.out.println("测试7 通过");
            pass++;
        }else{
            System.out.println("测试7 失败");
            fail++;
        }

        System.out.printf("自测结束：一共%d个用例，通过%d个，失败%d个%n", pass+fail, pass, fail);
    }

    // 读取文件全部文本
    public static String readFile(String filePath) throws IOException {
        return Files.readString(Paths.get(filePath));
    }

    // 生成3-gram集合
    public static Set<String> get3GramSet(String text) {
        Set<String> gramSet = new HashSet<>();
        // 去除全部空白
        String cleanText = text.replaceAll("\\s+", "");
        int len = cleanText.length();
        if (len < 3) {
            return gramSet;
        }
        for (int i = 0; i <= len - 3; i++) {
            String gram = cleanText.substring(i, i + 3);
            gramSet.add(gram);
        }
        return gramSet;
    }

    // 计算Jaccard相似度 J(A,B) = |A∩B| / |A∪B|
    public static double calcJaccard(Set<String> setA, Set<String> setB) {
        if (setA.isEmpty() && setB.isEmpty()) {
            return 1.0;
        }
        // 遍历更小集合，减少循环次数
        Set<String> smallSet = setA.size() < setB.size() ? setA : setB;
        Set<String> bigSet = setA.size() < setB.size() ? setB : setA;

        int intersection = 0;
        for (String s : smallSet) {
            if (bigSet.contains(s)) {
                intersection++;
            }
        }
        int union = setA.size() + setB.size() - intersection;
        return (double) intersection / union;
    }

    // 写入结果文件
    public static void writeResult(String outPath, double rate) throws IOException {
        String content = "相似度：" + String.format("%.2f%%", rate * 100);
        Files.writeString(Paths.get(outPath), content);
    }
}
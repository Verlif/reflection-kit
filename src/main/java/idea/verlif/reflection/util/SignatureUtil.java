package idea.verlif.reflection.util;

import idea.verlif.reflection.domain.ClassGrc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 签名工具类
 */
public class SignatureUtil {

    public static String getSignature(Object o) throws IllegalAccessException {
        Field signature;
        try {
            signature = o.getClass().getDeclaredField("signature");
        } catch (NoSuchFieldException e) {
            return null;
        }
        boolean acc = signature.isAccessible();
        if (!acc) {
            signature.setAccessible(true);
        }
        String sig = (String) signature.get(o);
        if (!acc) {
            signature.setAccessible(false);
        }
        return sig;
    }

    public static ClassGrc parseClassBySignature(String signature, Map<String, ClassGrc> genericsMap) throws ClassNotFoundException {
        // 去除尾部可能有的分号
        if (signature.charAt(signature.length() - 1) == ';') {
            signature = signature.substring(0, signature.length() - 1);
        }
        // 判断是否有泛型定义
        int tag = signature.indexOf('<');
        // 不含有泛型
        if (tag < 0) {
            // 判断是否是数组
            if (signature.charAt(0) == '[') {
                int i = 1;
                while (signature.charAt(i) == '[') {
                    i++;
                }
                Class<?> cl;
                if (signature.charAt(i) == 'L') {
                    cl = Class.forName(signature + ";");
                } else if (signature.charAt(i) == 'T') {
                    return getClassGrcFromKey(signature.substring(i + 1), genericsMap);
                } else {
                    cl = Class.forName(signature);
                }
                return new ClassGrc(cl);
            }
            // 判断自身是否是泛型
            if (signature.charAt(0) == 'T' || signature.charAt(0) == '*') {
                signature = signature.substring(1);
                return getClassGrcFromKey(signature, genericsMap);
            } else {
                signature = signature.substring(1).replace("/", ".");
                Class<?> cl = Class.forName(signature);
                return new ClassGrc(cl);
            }
        } else {
            String tarName = signature.substring(1, tag).replace("/", ".");
            String genericsStr = signature.substring(tag + 1, signature.length() - 1);
            List<String> genericsNames = splitSignature(genericsStr);
            ClassGrc[] genericsInfo = new ClassGrc[genericsNames.size()];
            for (int i = 0; i < genericsNames.size(); i++) {
                if (genericsNames.get(i).length() > 0) {
                    genericsInfo[i] = parseClassBySignature(genericsNames.get(i), genericsMap);
                }
            }
            Class<?> target = Class.forName(tarName);
            return new ClassGrc(target, genericsInfo);
        }
    }

    public static List<String> splitSignature(String signatureStr) {
        List<String> list = new ArrayList<>();
        if (signatureStr.indexOf('<') == -1) {
            Collections.addAll(list, signatureStr.split(";"));
        } else {
            char[] chars = signatureStr.toCharArray();
            int count = 0;
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (c == ';' && count == 0) {
                    if (i == chars.length - 1) {
                        break;
                    } else {
                        list.add(signatureStr.substring(0, i++));
                        signatureStr = signatureStr.substring(i);
                    }
                } else if (c == '<') {
                    count++;
                } else if (c == '>') {
                    count--;
                }
            }
            if (signatureStr.length() > 0) {
                list.add(signatureStr);
            }
        }
        return list;
    }

    private static ClassGrc getClassGrcFromKey(String key, Map<String, ClassGrc> genericsMap) {
        ClassGrc classGrc = genericsMap.get(key);
        if (classGrc == null) {
            classGrc = new ClassGrc();
            genericsMap.put(key, classGrc);
        }
        return classGrc;
    }
}

package com.sapfir.helpers;


import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class Workshop {
    public static void main(String[] args) {
        String[] compArray = new String[50];
        compArray[0] = "Pliskova Kr. - Yastremska D.";
        compArray[1] = "Wang Q. - Novosibirsk";
        compArray[2] = "Yastremska D. - Zhang S.";
        compArray[3] = "Svitolina E. - Wang Q.";
        compArray[4] = "Muguruza G. - Kumkhum L.";
        compArray[5] = "Pliskova Ka. - Zhang S.";
        compArray[6] = "Yastremska D. - Kucova K.";
        compArray[7] = "Svitolina E. - Hibino N.";
        compArray[8] = "McHale C. - Wang Q.";
        compArray[9] = "Kerkhove L. - Zhang S.";
        compArray[10] = "Gavrilova D. - Niculescu M.";
        compArray[11] = "Muguruza G. - Bogdan A.";
        compArray[12] = "Tomova V. - Kucova K.";
        compArray[13] = "Zheng S. - Yastremska D.";
        compArray[14] = "Kumkhum L. - Cornet A.";
        compArray[15] = "Svitolina E. - Hon P.";
        compArray[16] = "Muguruza G. - Sorribes Tormo S.";
        compArray[17] = "Dolehide C. - Bogdan A.";
        compArray[18] = "Sharipova S. - Zhang S.";
        compArray[19] = "Schoofs B. - Kerkhove L.";
        compArray[20] = "Stosur S. - Hibino N.";
        compArray[21] = "Zhang L. - Wang Q.";
        compArray[22] = "Gavrilova D. - Diyas Z.";
        compArray[23] = "McHale C. - Chong E. W.";
        compArray[24] = "Niculescu M. - Jabeur O.";
        compArray[25] = "Krunic A. - Zheng S.";
        compArray[26] = "Kucova K. - Ostapenko J.";
        compArray[27] = "Glushko J. - Cornet A.";
        compArray[28] = "Brady J. - Kumkhum L.";
        compArray[29] = "Tomova V. - Dynamo Kyiv";
        compArray[30] = "Yastremska D. - Stollar F.";
        compArray[31] = "Niculescu M. - Makarova E.";
        compArray[32] = "Stosur S. - Krunic A.";
        compArray[33] = "Tsurenko L. - Zheng S.";
        compArray[34] = "Shakhtar Donetsk - Cornet A.";
        compArray[35] = "Hibino N. - Zhao C.";
        compArray[36] = "Dolehide C. - Kerkhove L.";
        compArray[37] = "Stollar F. - Siegemund L.";
        compArray[38] = "Lu J. - Sharipova S.";
        compArray[39] = "Glushko J. - Schoofs B.";
        compArray[40] = "Jabeur O. - Tomova V.";
        compArray[41] = "Dolehide C. - Wu Ho C.";
        compArray[42] = "Melnikova M. - Sharipova S.";
        compArray[43] = "D. Zagreb - Ng K. Y.";
        compArray[44] = "Kerkhove L. - Liu C.";
        compArray[45] = "Glushko J. - Siegemund L.";
        compArray[46] = "Zhao C. - Mrdeza T.";
        compArray[47] = "Ng M. - Schoofs B.";
        compArray[48] = "Watson H. - FK Crvena zvezda";
        compArray[49] = "Hibino N. - Shimizu A.";

        String betTeam = "Shakhtar".replace(".", "");

        boolean matchFound = false;
        int indexFound = -1;

        String[] words = betTeam.split(" ");
        int j = 0;
        while (!matchFound && j < words.length) {
            char[] letters = words[j].toCharArray();
            int k = letters.length - 1;
            while (!matchFound && k >= 0) {
                String newWord = words[j].substring(0, k);
                int i = 0;
                while (!matchFound && i < compArray.length) {
                    String competitorsLine = compArray[i].replace(".", "");
                    matchFound = competitorsLine.contains(betTeam.replace(words[j], newWord));
                    if (matchFound) {
                        indexFound = i;
                    }
                    i++;
                }
                k--;
            }
            j++;
        }
        System.out.println(matchFound + " " + indexFound);
    }
}

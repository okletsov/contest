package com.sapfir.helpers;


import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class Workshop {
    public static void main(String[] args) {
        String[] competitorsElements = new String[50];
        competitorsElements[0] = "Pliskova Kr. - Yastremska D.";
        competitorsElements[1] = "Wang Q. - Novosibirsk";
        competitorsElements[2] = "Yastremska D. - Zhang S.";
        competitorsElements[3] = "Svitolina E. - Wang Q.";
        competitorsElements[4] = "Muguruza G. - Kumkhum L.";
        competitorsElements[5] = "Pliskova Ka. - Zhang S.";
        competitorsElements[6] = "Yastremska D. - Kucova K.";
        competitorsElements[7] = "Svitolina E. - Hibino N.";
        competitorsElements[8] = "McHale C. - Wang Q.";
        competitorsElements[9] = "Kerkhove L. - Zhang S.";
        competitorsElements[10] = "Gavrilova D. - Niculescu M.";
        competitorsElements[11] = "Muguruza G. - Bogdan A.";
        competitorsElements[12] = "Tomova V. - Kucova K.";
        competitorsElements[13] = "Zheng S. - Yastremska D.";
        competitorsElements[14] = "Kumkhum L. - Cornet A.";
        competitorsElements[15] = "Svitolina E. - Hon P.";
        competitorsElements[16] = "Muguruza G. - Sorribes Tormo S.";
        competitorsElements[17] = "Dolehide C. - Bogdan A.";
        competitorsElements[18] = "Sharipova S. - Zhang S.";
        competitorsElements[19] = "Schoofs B. - Kerkhove L.";
        competitorsElements[20] = "Stosur S. - Hibino N.";
        competitorsElements[21] = "Zhang L. - Wang Q.";
        competitorsElements[22] = "Gavrilova D. - Diyas Z.";
        competitorsElements[23] = "McHale C. - Chong E. W.";
        competitorsElements[24] = "Niculescu M. - Jabeur O.";
        competitorsElements[25] = "Krunic A. - Zheng S.";
        competitorsElements[26] = "Kucova K. - Ostapenko J.";
        competitorsElements[27] = "Glushko J. - Cornet A.";
        competitorsElements[28] = "Brady J. - Kumkhum L.";
        competitorsElements[29] = "Tomova V. - Dynamo Kyiv";
        competitorsElements[30] = "Yastremska D. - Stollar F.";
        competitorsElements[31] = "Niculescu M. - Makarova E.";
        competitorsElements[32] = "Stosur S. - Krunic A.";
        competitorsElements[33] = "Tsurenko L. - Zheng S.";
        competitorsElements[34] = "Shakhtar Donetsk - Cornet A.";
        competitorsElements[35] = "Hibino N. - Zhao C.";
        competitorsElements[36] = "Dolehide C. - Kerkhove L.";
        competitorsElements[37] = "Stollar F. - Siegemund L.";
        competitorsElements[38] = "Lu J. - Sharipova S.";
        competitorsElements[39] = "Glushko J. - Schoofs B.";
        competitorsElements[40] = "Jabeur O. - Tomova V.";
        competitorsElements[41] = "Dolehide C. - Wu Ho C.";
        competitorsElements[42] = "Melnikova M. - Sharipova S.";
        competitorsElements[43] = "D. Zagreb - Ng K. Y.";
        competitorsElements[44] = "Kerkhove L. - Liu C.";
        competitorsElements[45] = "Glushko J. - Siegemund L.";
        competitorsElements[46] = "Zhao C. - Mrdeza T.";
        competitorsElements[47] = "Ng M. - Schoofs B.";
        competitorsElements[48] = "Watson H. - FK Crvena zvezda";
        competitorsElements[49] = "Hibino N. - Shimizu A.";

        String winnerPredicted = "Shakhtar".replace(".", "");

        int gameIndex = -1;
        boolean matchFound = false;

        String[] words = winnerPredicted.split(" ");
        int j = 0;
        while (!matchFound && j < words.length) {
            char[] letters = words[j].toCharArray();
            int k = letters.length - 1;
            while (!matchFound && k >= 0) {
                String newWord = words[j].substring(0, k);
                int i = 0;
                while (!matchFound && i < competitorsElements.length) {
                    String gameCompetitors = competitorsElements[i].replace(".", "");
                    matchFound = gameCompetitors.contains(winnerPredicted.replace(words[j], newWord));
                    if (matchFound) {
                        gameIndex = i;
                    }
                    i++;
                }
                k--;
            }
            j++;
        }
        System.out.println(matchFound + " " + gameIndex);
    }
}

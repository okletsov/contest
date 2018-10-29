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
        compArray[29] = "Tomova V. - Jakupovic D.";
        compArray[30] = "Yastremska D. - Stollar F.";
        compArray[31] = "Niculescu M. - Makarova E.";
        compArray[32] = "Stosur S. - Krunic A.";
        compArray[33] = "Tsurenko L. - Zheng S.";
        compArray[34] = "Zhang S. - Cornet A.";
        compArray[35] = "Hibino N. - Zhao C.";
        compArray[36] = "Dolehide C. - Kerkhove L.";
        compArray[37] = "Stollar F. - Siegemund L.";
        compArray[38] = "Lu J. - Sharipova S.";
        compArray[39] = "Glushko J. - Schoofs B.";
        compArray[40] = "Jabeur O. - Tomova V.";
        compArray[41] = "Dolehide C. - Wu Ho C.";
        compArray[42] = "Melnikova M. - Sharipova S.";
        compArray[43] = "Stollar F. - Ng K. Y.";
        compArray[44] = "Kerkhove L. - Liu C.";
        compArray[45] = "Shakhtar Donetsk - Siegemund L.";
        compArray[46] = "Zhao C. - Mrdeza T.";
        compArray[47] = "Ng M. - Schoofs B.";
        compArray[48] = "Watson H. - FK Crvena zvezda";
        compArray[49] = "Hibino N. - Shimizu A.";

        /*
            Variables:
                betTeam - the team/person user predicted to win a tournament (OUTRIGHTS market)
                compArray - array storing event results for the tournament

            Goal:
                find index (first occurrence) of betTeam in compArray.
                The index will be used to determine the datetime event occurred

            How method works:
                Because team name in OUTRIGHTS and tournament RESULTS pages not always match, method will
                do the following:

                if betText consist of only one word the method will try to find that word in compaArray
                if betText consist of > 1 words:
                    1) it will try to find betText in compArray, if mno match found then
                    2) it will go word by word in betText and will:
                        - replace the word in betText with empty string --> try to find match in compAtrray
                        - replace the word in betText with its first letter --> try to find match in compAtrray
                        - replace the word in betText with its first two letters --> try to find match in compAtrray
                        - replace the word in betText with its first three letters --> try to find match in compAtrray
                        - replace the word in betText with its first four letters --> try to find match in compAtrray
                        - ...

             Can return wrong index if:
                - betText consists of > 1 words
                  AND one of the words shortened
                  AND there are two teams with same NOT shortened word playing in tournament

                  Example: betText can be Williams Serena or Williams Venus
                           compArray is shortened to Williams S. and Williams. V
                           method will return wrong result if user predicted Williams Serena to win, but Venus advanced
                           to later stages (her games were after last Serena's) comparing to Serena

            Think how to handle Pliskova example above
             - user bets on Karolina, but Kristina wins (method will return Kristina's result)
         */
        String betTeam = "Shakhtar";

        boolean matchFound = false;
        int i = 0;
        while (!matchFound && i < compArray.length) {
            String competitorsLine = compArray[i].replace(".", "");
            matchFound = competitorsLine.contains(betTeam);
            String[] words = betTeam.split(" ");
            if (!matchFound && words.length > 1) {
                int j = 0;
                while (!matchFound && j < words.length) {
                    char[] letters = words[j].toCharArray();
                    int k = 0;
                    while (!matchFound && k < letters.length) {
                        String newWord = words[j].substring(0, k);
                        matchFound = competitorsLine.contains(betTeam.replace(words[j], newWord));
                        k++;
                    }
                    j++;
                }
            }
            i++;
        }

        int indexFound = i - 1;
        System.out.println(matchFound + " " + indexFound);
    }
}

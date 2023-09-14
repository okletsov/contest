package com.sapfir.Sandbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonSandbox {

    public static void main(final String... args) throws IOException {

        String foundUserId = "";
        String usernameToSearchFor = "Cap4ik";
        String jsonString = "{\"s\":1,\"d\":{\"users\":[{\"userId\":7052001,\"clearUserId\":70520,\"notify\":0,\"followingFLag\":1},{\"userId\":7065301,\"clearUserId\":70653,\"notify\":0,\"followingFLag\":1},{\"userId\":12273701,\"clearUserId\":122737,\"notify\":0,\"followingFLag\":1},{\"userId\":12279801,\"clearUserId\":122798,\"notify\":0,\"followingFLag\":1},{\"userId\":12284801,\"clearUserId\":122848,\"notify\":0,\"followingFLag\":1},{\"userId\":12354201,\"clearUserId\":123542,\"notify\":0,\"followingFLag\":1},{\"userId\":13819701,\"clearUserId\":138197,\"notify\":0,\"followingFLag\":1},{\"userId\":16641001,\"clearUserId\":166410,\"notify\":0,\"followingFLag\":1},{\"userId\":31021001,\"clearUserId\":310210,\"notify\":0,\"followingFLag\":1},{\"userId\":31995301,\"clearUserId\":319953,\"notify\":0,\"followingFLag\":1},{\"userId\":32053201,\"clearUserId\":320532,\"notify\":0,\"followingFLag\":1},{\"userId\":32067301,\"clearUserId\":320673,\"notify\":0,\"followingFLag\":1},{\"userId\":32167501,\"clearUserId\":321675,\"notify\":0,\"followingFLag\":1}],\"images\":{\"7052001\":\"https:\\/\\/www.oddsportal.com\\/serve\\/u\\/1_1_70520_60d730f4be7d923c3d4728144de31d60.jpg?1694302425\",\"7065301\":\"https:\\/\\/www.oddsportal.com\\/serve\\/u\\/1_1_70653_447608a90ca2972bed63fe379c459175.jpg?1694302425\",\"12273701\":\"https:\\/\\/www.oddsportal.com\\/serve\\/u\\/1_1_122737_dcd20c458ae9b6a3f054763aea9d92ff.jpg?1694302425\",\"12279801\":\"https:\\/\\/www.oddsportal.com\\/serve\\/u\\/1_1_122798_3a8cccf6da7e47b36f6efa1b16f4f38e.jpg?1694302425\",\"12284801\":\"https:\\/\\/www.oddsportal.com\\/serve\\/u\\/1_1_122848_a3e62f27a18ed27728f4e47bfbc77859.jpg?1694302425\",\"12354201\":\"https:\\/\\/www.oddsportal.com\\/serve\\/u\\/1_1_123542_832f870e06f5d1f2be1f85ed96dc5465.jpg?1694302425\",\"31021001\":\"https:\\/\\/www.oddsportal.com\\/serve\\/u\\/1_1_310210_00f19f1f008c100860e3b067677ac920.jpg?1694302425\",\"31995301\":\"https:\\/\\/www.oddsportal.com\\/serve\\/u\\/1_1_319953_075362a05500b3b032b74e29723307c9.jpg?1694302425\",\"32053201\":\"https:\\/\\/www.oddsportal.com\\/serve\\/u\\/1_1_320532_b47b20e174632857c1ddbeab7d99b1a7.jpg?1694302425\",\"32067301\":\"https:\\/\\/www.oddsportal.com\\/serve\\/u\\/1_1_320673_1f7fab1780a3f863385fb73d370c6bf1.jpg?1694302425\",\"32167501\":\"https:\\/\\/www.oddsportal.com\\/serve\\/u\\/1_1_321675_1577812b766e743c3a121b724a30be7a.jpg?1694302425\",\"13819701\":\"\\/res\\/public\\/images\\/big_user_icon.svg\",\"16641001\":\"\\/res\\/public\\/images\\/big_user_icon.svg\",\"default\":true},\"info\":{\"7052001\":{\"Username\":\"Cap4ik\",\"Url\":\"\\/profile\\/Cap4ik\\/\",\"Roi\":-15.699999999999999289457264239899814128875732421875,\"CountryId\":195,\"CountryName\":\"Ukraine\",\"countryTwoCharCode\":\"ua\",\"status\":1,\"privacy\":true,\"banned\":false,\"isOwner\":false,\"isFollowed\":true},\"7065301\":{\"Username\":\"Dnepr\",\"Url\":\"\\/profile\\/Dnepr\\/\",\"Roi\":12.9000000000000003552713678800500929355621337890625,\"CountryId\":195,\"CountryName\":\"Ukraine\",\"countryTwoCharCode\":\"ua\",\"status\":1,\"privacy\":true,\"banned\":false,\"isOwner\":false,\"isFollowed\":true},\"12273701\":{\"Username\":\"BeTeLGeuSe\",\"Url\":\"\\/profile\\/BeTeLGeuSe\\/\",\"Roi\":17.800000000000000710542735760100185871124267578125,\"CountryId\":158,\"CountryName\":\"Russia\",\"countryTwoCharCode\":\"ru\",\"status\":1,\"privacy\":true,\"banned\":false,\"isOwner\":false,\"isFollowed\":true},\"12279801\":{\"Username\":\"Deagle\",\"Url\":\"\\/profile\\/Deagle\\/\",\"Roi\":11.300000000000000710542735760100185871124267578125,\"CountryId\":200,\"CountryName\":\"USA\",\"countryTwoCharCode\":\"us\",\"status\":1,\"privacy\":true,\"banned\":false,\"isOwner\":false,\"isFollowed\":true},\"12284801\":{\"Username\":\"stan507\",\"Url\":\"\\/profile\\/stan507\\/\",\"Roi\":15.4000000000000003552713678800500929355621337890625,\"CountryId\":195,\"CountryName\":\"Ukraine\",\"countryTwoCharCode\":\"ua\",\"status\":1,\"privacy\":true,\"banned\":false,\"isOwner\":false,\"isFollowed\":true},\"12354201\":{\"Username\":\"ka1manua\",\"Url\":\"\\/profile\\/ka1manua\\/\",\"Roi\":-9.0999999999999996447286321199499070644378662109375,\"CountryId\":195,\"CountryName\":\"Ukraine\",\"countryTwoCharCode\":\"ua\",\"status\":1,\"privacy\":true,\"banned\":false,\"isOwner\":false,\"isFollowed\":true},\"13819701\":{\"Username\":\"Ajax\",\"Url\":\"\\/profile\\/Ajax\\/\",\"Roi\":46.39999999999999857891452847979962825775146484375,\"CountryId\":195,\"CountryName\":\"Ukraine\",\"countryTwoCharCode\":\"ua\",\"status\":1,\"privacy\":true,\"banned\":false,\"isOwner\":false,\"isFollowed\":true},\"16641001\":{\"Username\":\"gorgEuro\",\"Url\":\"\\/profile\\/gorgEuro\\/\",\"Roi\":-6.9000000000000003552713678800500929355621337890625,\"CountryId\":195,\"CountryName\":\"Ukraine\",\"countryTwoCharCode\":\"ua\",\"status\":1,\"privacy\":true,\"banned\":false,\"isOwner\":false,\"isFollowed\":true},\"31021001\":{\"Username\":\"DavidsKV\",\"Url\":\"\\/profile\\/DavidsKV\\/\",\"Roi\":-41.10000000000000142108547152020037174224853515625,\"CountryId\":158,\"CountryName\":\"Russia\",\"countryTwoCharCode\":\"ru\",\"status\":1,\"privacy\":true,\"banned\":false,\"isOwner\":false,\"isFollowed\":true},\"31995301\":{\"Username\":\"Zizu\",\"Url\":\"\\/profile\\/Zizu\\/\",\"Roi\":26.800000000000000710542735760100185871124267578125,\"CountryId\":195,\"CountryName\":\"Ukraine\",\"countryTwoCharCode\":\"ua\",\"status\":1,\"privacy\":true,\"banned\":false,\"isOwner\":false,\"isFollowed\":true},\"32053201\":{\"Username\":\"JovtoBlakutni\",\"Url\":\"\\/profile\\/JovtoBlakutni\\/\",\"Roi\":1.100000000000000088817841970012523233890533447265625,\"CountryId\":158,\"CountryName\":\"Russia\",\"countryTwoCharCode\":\"ru\",\"status\":1,\"privacy\":true,\"banned\":false,\"isOwner\":false,\"isFollowed\":true},\"32067301\":{\"Username\":\"Toha\",\"Url\":\"\\/profile\\/Toha\\/\",\"Roi\":-47.10000000000000142108547152020037174224853515625,\"CountryId\":158,\"CountryName\":\"Russia\",\"countryTwoCharCode\":\"ru\",\"status\":1,\"privacy\":true,\"banned\":false,\"isOwner\":false,\"isFollowed\":true},\"32167501\":{\"Username\":\"Sayda\",\"Url\":\"\\/profile\\/Sayda\\/\",\"Roi\":-27,\"CountryId\":158,\"CountryName\":\"Russia\",\"countryTwoCharCode\":\"ru\",\"status\":1,\"privacy\":true,\"banned\":false,\"isOwner\":false,\"isFollowed\":true}}},\"refresh\":20}";

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonString);

            JsonNode usersInfo = rootNode.at("/d/info");

            List<String> userIds = new ArrayList<>();
            Iterator<String> iterator = usersInfo.fieldNames();
            iterator.forEachRemaining(userIds::add);

            for (String individualUserid: userIds) {
                JsonNode userDetails = rootNode.at("/d/info/" + individualUserid);

                String loopUsername = userDetails.get("Username").toString().replaceAll("\"", "");
                System.out.println(loopUsername);

                if (usernameToSearchFor.equals(loopUsername)) {
                    foundUserId = individualUserid;
                }
            }

        }  catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(jsonString);
    }
}

package searchengine;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.services.ConvertingWordsIntoLemmas;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class MainTest {

    private static String testString = "Повторное появление леопарда в Осетии позволяет предположить, " +
            "что леопард постоянно обитает в некоторых районах Северного Кавказа.";

    private static String testHtml = "<a class=\"tele_span\" href=\"/\"><span class=\"tele_span_playback\">PlayBack.ru</span></a><a href=\"tel:+74951437771\">8(495)143-77-71</a><a href=\"/basket.html\"><img src=\"/img_new/basket.png\" width=\"49px\" border=\"0\"></a><a class=\"tele_span2\" href=\"/basket.html\">Корзина</a><a href=\"\" class=\"active\" onclick=\"return false;\"><img src=\"/img/imglist.png\" height=\"9px\"> Каталог<span class=\"fa fa-angle-down\"></span></a><a href=\"/catalog/1652.html\">Чехлы для смартфонов Infinix</a><a href=\"/catalog/1511.html\">Смартфоны</a><a href=\"/catalog/1300.html\">Чехлы для смартфонов Xiaomi</a><a href=\"/catalog/1302.html\">Защитные стекла для смартфонов Xiaomi</a><a href=\"/catalog/1310.html\">Чехлы для Huawei/Honor</a><a href=\"/catalog/1308.html\">Чехлы для смартфонов Samsung</a><a href=\"/catalog/1307.html\">Защитные стекла для смартфонов Samsung</a><a href=\"/catalog/1141.html\">Планшеты</a><a href=\"/catalog/1315.html\">Зарядные устройства и кабели</a><a href=\"/catalog/1329.html\">Держатели для смартфонов</a><a href=\"/catalog/665.html\">Автодержатели</a><a href=\"/catalog/1304.html\">Носимая электроника</a><a href=\"/catalog/1305.html\">Наушники и колонки</a><a href=\"/catalog/1314.html\">Гаджеты Xiaomi</a><a href=\"/catalog/805.html\">Запчасти для телефонов</a><a href=\"/catalog/1311.html\">Чехлы для планшетов</a><a href=\"/catalog/1317.html\">Аксессуары для фото-видео</a><a href=\"/catalog/1318.html\">Чехлы для смартфонов Apple</a><a href=\"/catalog/1429.html\">USB Флеш-накопители</a><a href=\"/catalog/1473.html\">Товары для детей</a><a href=\"/catalog/1507.html\">Защитные стекла для смартфонов Realme</a><a href=\"/catalog/1508.html\">Чехлы для смартфонов Realme</a><a href=\"/catalog/18.html\">Карты памяти</a><a href=\"/catalog/1303.html\">Защитные стекла для планшетов</a><a href=\"/catalog/1312.html\">Защитные стекла для смартфонов</a><a href=\"/catalog/1622.html\">Защитные стекла для смартфонов Apple</a><a href=\"/catalog/1626.html\">Чехлы для смартфонов Vivo</a><a href=\"/catalog/1636.html\">Чехлы для смартфонов Tecno</a><a href=\"/dostavka.html\">Доставка</a><a href=\"/pickup.html\">Самовывоз</a><a href=\"/payment.html\">Оплата</a><a href=\"/warranty.html\">Гарантия и обмен</a><a href=\"/contacts.html\">Контакты</a><a href=\"/product/1125199.html\" title=\"Описание и характеристики Смартфон Samsung Galaxy A55 5G 8/256 ГБ темно-синий (Global Version)\">Смартфон Samsung Galaxy A55 5G 8/256 ГБ темно-синий (Global Version)</a><a href=\"/product/1125416.html\" title=\"Описание и характеристики Смартфон Vivo Y36 8/256 ГБ, лазурное море\">Смартфон Vivo Y36 8/256 ГБ, лазурное море</a><a href=\"/product/1124768.html\" title=\"Описание и характеристики Смартфон Xiaomi 13T 8/256 ГБ черный (РСТ)\">Смартфон Xiaomi 13T 8/256 ГБ черный (РСТ)</a><a href=\"/product/1123982.html\" title=\"Описание и характеристики Смартфон Xiaomi Redmi Note 12 Pro 4G 8/256 ГБ голубой (Global Version)\">Смартфон Xiaomi Redmi Note 12 Pro 4G 8/256 ГБ голубой (Global Version)</a><a href=\"/product/1123979.html\" title=\"Описание и характеристики Смартфон Xiaomi Redmi Note 12 Pro 4G 8/256 ГБ голубой (РСТ)\">Смартфон Xiaomi Redmi Note 12 Pro 4G 8/256 ГБ голубой (РСТ)</a><a href=\"/product/1123981.html\" title=\"Описание и характеристики Смартфон Xiaomi Redmi Note 12 Pro 4G 8/256 ГБ серый (Global Version)\">Смартфон Xiaomi Redmi Note 12 Pro 4G 8/256 ГБ серый (Global Version)</a><a href=\"/\">Наши спецпредложения</a><a href=\"/dostavka.html\">Доставка</a><a href=\"/payment.html\">Оплата</a><a href=\"/warranty.html\">Гарантия</a><a href=\"/contacts.html\">Контакты</a><a href=\"/privacy_policy.html\">Положение о конфиденциальности и защите персональных данных</a><a class=\"footer_email\" href=\"http://vk.com/playback_ru\" target=\"_blank\"><img src=\"/img/VK.png\" title=\"Наша страница Вконтакте\"></a><a href=\"#\" class=\"scrollup\">Наверх</a>";


    public static void main(String[] args) throws IOException {



        ConvertingWordsIntoLemmas convertingWordsIntoLemmas =
                new ConvertingWordsIntoLemmas();


        HashMap<String, Integer> testMap =
                convertingWordsIntoLemmas.convertingIntoLemmas(testString);

        Set<String> testSet =
                convertingWordsIntoLemmas.getLemmaSet(testString);

        String clearHtml =
                convertingWordsIntoLemmas.removeHtmlTag(testHtml);

        System.out.println(testMap);
        System.out.println(clearHtml);





    }
}

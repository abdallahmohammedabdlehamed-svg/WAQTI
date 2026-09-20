package com.example.data.spiritual

data class QuranVerse(
    val surahNameAr: String,
    val surahNameEn: String,
    val ayahNumber: Int,
    val arabicText: String,
    val englishTranslation: String,
    val reference: String
)

data class AzkarItem(
    val id: String,
    val textAr: String,
    val textEn: String,
    val targetCount: Int,
    val benefitAr: String,
    val benefitEn: String
)

object QuranAzkarData {
    val authenticVerses: List<QuranVerse> = listOf(
        QuranVerse(
            surahNameAr = "سورة الانشراح",
            surahNameEn = "Ash-Sharh",
            ayahNumber = 5,
            arabicText = "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا",
            englishTranslation = "For indeed, with hardship [will be] ease.",
            reference = "Quran 94:5"
        ),
        QuranVerse(
            surahNameAr = "سورة البقرة",
            surahNameEn = "Al-Baqarah",
            ayahNumber = 152,
            arabicText = "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
            englishTranslation = "So remember Me; I will remember you. And be grateful to Me and do not deny Me.",
            reference = "Quran 2:152"
        ),
        QuranVerse(
            surahNameAr = "سورة طه",
            surahNameEn = "Taha",
            ayahNumber = 114,
            arabicText = "وَقُل رَّبِّ زِدْنِي عِلْمًا",
            englishTranslation = "And say, 'My Lord, increase me in knowledge.'",
            reference = "Quran 20:114"
        ),
        QuranVerse(
            surahNameAr = "سورة الرعد",
            surahNameEn = "Ar-Ra'd",
            ayahNumber = 28,
            arabicText = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            englishTranslation = "Unquestionably, by the remembrance of Allah hearts are assured.",
            reference = "Quran 13:28"
        ),
        QuranVerse(
            surahNameAr = "سورة النجم",
            surahNameEn = "An-Najm",
            ayahNumber = 39,
            arabicText = "وَأَن لَّيْسَ لِلْإِنسَانِ إِلَّا مَا سَعَىٰ",
            englishTranslation = "And that there is not for man except that [good] for which he strives.",
            reference = "Quran 53:39"
        ),
        QuranVerse(
            surahNameAr = "سورة الطلاق",
            surahNameEn = "At-Talaq",
            ayahNumber = 3,
            arabicText = "وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ",
            englishTranslation = "And whoever relies upon Allah - then He is sufficient for him.",
            reference = "Quran 65:3"
        )
    )

    val morningAzkarList: List<AzkarItem> = listOf(
        AzkarItem(
            id = "m1",
            textAr = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ.",
            textEn = "We have entered the morning and the kingdom belongs to Allah; praise is due to Allah, none has the right to be worshipped except Allah alone.",
            targetCount = 1,
            benefitAr = "بداية اليوم بتوحيد الله والاعتراف بملكه وفضله",
            benefitEn = "Starting the morning with affirmation of oneness and gratitude"
        ),
        AzkarItem(
            id = "m2",
            textAr = "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ النُّشُورُ.",
            textEn = "O Allah, by You we enter the morning and by You we enter the evening, by You we live and by You we die, and to You is the resurrection.",
            targetCount = 1,
            benefitAr = "استيداع النفس لله والتوكل عليه",
            benefitEn = "Entrusting oneself to the Creator throughout the day"
        ),
        AzkarItem(
            id = "m3",
            textAr = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ.",
            textEn = "Glory be to Allah and His is the praise, according to the number of His creation, and according to His good pleasure, and to the weight of His Throne, and the ink of His words.",
            targetCount = 3,
            benefitAr = "أجر عظيم يعادل أوقاتًا طويلة من الذكر",
            benefitEn = "Vast reward equating lengthy periods of contemplation"
        ),
        AzkarItem(
            id = "m4",
            textAr = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.",
            textEn = "In the name of Allah with whose Name nothing in earth or heaven can cause harm, and He is the All-Hearing, All-Knowing.",
            targetCount = 3,
            benefitAr = "حفظ وحماية من كل سوء طوال النهار",
            benefitEn = "Protection and peace of mind throughout the day"
        )
    )

    val eveningAzkarList: List<AzkarItem> = listOf(
        AzkarItem(
            id = "e1",
            textAr = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ.",
            textEn = "We have entered the evening and the kingdom belongs to Allah; praise is due to Allah.",
            targetCount = 1,
            benefitAr = "استقبال المساء بالحمد والسكينة",
            benefitEn = "Welcoming the evening with serenity and gratitude"
        ),
        AzkarItem(
            id = "e2",
            textAr = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ.",
            textEn = "I seek refuge in the perfect words of Allah from the evil of what He has created.",
            targetCount = 3,
            benefitAr = "حفظ الليل والهدوء النفسي",
            benefitEn = "Evening protection and tranquility"
        ),
        AzkarItem(
            id = "e3",
            textAr = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ.",
            textEn = "O Allah, I ask You for pardon and well-being in this world and the Hereafter.",
            targetCount = 1,
            benefitAr = "دعاء شامل لراحة البال والصحة والسلامة",
            benefitEn = "Comprehensive prayer for peace, health, and wellness"
        )
    )

    val sleepAzkarList: List<AzkarItem> = listOf(
        AzkarItem(
            id = "s1",
            textAr = "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي، وَبِكَ أَرْفَعُهُ، فَإِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ.",
            textEn = "In Your name my Lord, I lie down, and by Your power I rise. If You take my soul, have mercy upon it, and if You release it, protect it as You protect Your righteous servants.",
            targetCount = 1,
            benefitAr = "أمان وسكينة وطمأنينة النوم على الفطرة",
            benefitEn = "Peace, comfort and spiritual shelter during sleep"
        ),
        AzkarItem(
            id = "s2",
            textAr = "اللَّهُمَّ خَلَقْتَ نَفْسِي وَأَنْتَ تَوَفَّاهَا، لَكَ مَمَاتُهَا وَمَحْيَاهَا، إِنْ أَحْيَيْتَهَا فَاحْفَظْهَا، وَإِنْ أَمَتَّهَا فَاغْفِرْ لَهَا.",
            textEn = "O Allah, You created my soul and You take it. To You belongs its life and death. If You preserve it, protect it, and if You take it, forgive it.",
            targetCount = 1,
            benefitAr = "تفويض الأمر إلى الله واستشعار حفظه",
            benefitEn = "Surrendering matters unto Allah with total trust"
        ),
        AzkarItem(
            id = "s3",
            textAr = "سُورَةُ الإِخْلَاصِ وَالمُعَوِّذَتَيْنِ (قُلْ هُوَ اللَّهُ أَحَدٌ، قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ، قُلْ أَعُوذُ بِرَبِّ النَّاسِ).",
            textEn = "Recitation of Surah Al-Ikhlas, Al-Falaq, and An-Nas.",
            targetCount = 3,
            benefitAr = "حفظ تام من الشياطين والوساوس والأحلام المزعجة",
            benefitEn = "Complete protection from negative whispers and nightmares"
        )
    )

    val afterPrayerAzkarList: List<AzkarItem> = listOf(
        AzkarItem(
            id = "p1",
            textAr = "أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ.. اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالإِكْرَامِ.",
            textEn = "I seek Allah's forgiveness (x3). O Allah, You are Peace and from You comes peace; blessed are You, Possessor of Glory and Honor.",
            targetCount = 1,
            benefitAr = "استفتاح أذكار ما بعد الفريضة بالاستغفار والثناء",
            benefitEn = "Seeking forgiveness and praising the Divine immediately after prayer"
        ),
        AzkarItem(
            id = "p2",
            textAr = "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ، وَشُكْرِكَ، وَحُسْنِ عِبَادَتِكَ.",
            textEn = "O Allah, help me to remember You, thank You, and worship You in the best manner.",
            targetCount = 1,
            benefitAr = "وصية نبوية جامعة للثبات والاستعانة بالله",
            benefitEn = "A comprehensive Prophetic prayer for steadfast devotion"
        ),
        AzkarItem(
            id = "p3",
            textAr = "سُبْحَانَ اللَّهِ (33)، الْحَمْدُ لِلَّهِ (33)، اللَّهُ أَكْبَرُ (33).. وتَمَامُ الْمِائَةِ: لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ.",
            textEn = "SubhanAllah (33x), Alhamdulillah (33x), Allahu Akbar (33x), completed with La ilaha illa Allah.",
            targetCount = 33,
            benefitAr = "غفران الخطايا ورفعة الدرجات في الميزان",
            benefitEn = "Forgiveness of missteps and great elevation in virtue"
        ),
        AzkarItem(
            id = "p4",
            textAr = "آيَةُ الْكُرْسِيِّ: ﴿ اللَّهُ لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ.. ﴾",
            textEn = "Ayat Al-Kursi (Quran 2:255).",
            targetCount = 1,
            benefitAr = "من قرأها دبر كل صلاة لم يمنعه من دخول الجنة إلا أن يموت",
            benefitEn = "One of the greatest assurances of Paradise when read after obligatory prayers"
        )
    )

    val dhikrPhrases = listOf(
        "سُبْحَانَ اللَّهِ" to "SubhanAllah (Glory be to Allah)",
        "الْحَمْدُ لِلَّهِ" to "Alhamdulillah (All praise is due to Allah)",
        "لَا إِلَهَ إِلَّا اللَّهُ" to "La ilaha illa Allah (None worthy of worship but Allah)",
        "اللَّهُ أَكْبَرُ" to "Allahu Akbar (Allah is Greater)",
        "أَسْتَغْفِرُ اللَّهَ" to "Astaghfirullah (I ask Allah for forgiveness)",
        "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ" to "La hawla wala quwwata illa billah",
        "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ" to "Allahumma Salli Ala Muhammad (Salawat)"
    )
}

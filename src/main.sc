require: patterns.sc
require: functions.js

theme: /Cinema
    state: Start
        q!: *start
        random:
            a: Привет!
            a: Рад видеть!
            a: Здравствуй!
        a: Я кинобот! Могу рассказать тебе о фильмах, которые идут сейчас в кино, а также помочь с покупкой билета. Что ты хочешь сделать?
        
    state: Hello
        q!: * (прив*/хай/ку/здравствуй*/здрасте/здрасьте/хэлоу/*даров*) *
        q!: * {~добрый * (~утро/~день/~вечер/~ночь/~время суток)} *
        random:
            a: Привет!
            a: Рад видеть!
            a: Здравствуй!
        random:
            a: Что ты хочешь сделать?
            a: Чем могу помочь?
            
    state: BotDescription
        q!: * {что (ты/вы) * (~мочь/~уметь/~делать)} *
        q!: * {(кто/что) * (ты/вы)} *
        a: Я кинобот! Могу рассказать тебе о фильмах, которые идут сейчас в кино, а также помочь с покупкой билета. Что ты хочешь сделать?
        
    state: StopPurchase
        q: * (все/стоп/хватит/отстань/снова/отмена) * || fromState = /Cinema/SuggestMovie
        q: * (все/стоп/хватит/отстань/снова/отмена) * || fromState = /Cinema/HowManyTickets
        q!: * (все/стоп/хватит/отстань/снова/отмена) *
        go!: /Cinema/Bye
    
    state: Plot
    # хочу сходить на пленницу, про что он
    # сюжет джентельменов удачи
    # о чем кавказская пленница?
    # хочу купить билет на кавказскую пленницу, о чем он?
        q!: * {~сюжет * ($GentlemenOfFortune/$CaucasianPrisoner)} *
        q!: * {(про/о) (что/чем/чём) * ($GentlemenOfFortune/$CaucasianPrisoner)} *
        script:
            $session.Movie = GetMovieName($parseTree)
            $session.Plot = GetMoviePlot($parseTree)
            var answer = "Сюжет фильма \"" + $session.Movie + "\":\n" + $session.Plot;
            $reactions.answer(answer);
        random:
            a: Нравится? Пойдешь смотреть?
            a: Звучит интересно! Берешь билеты?
        buttons:
            "Да"
            "Нет"
            
        state: GoodPlot
            q: * (да/ес/ага/агась/правильно/именно) *
            go!: /Cinema/HowManyTickets
        
        state: BadPlot
            q: * (не*) *
            random:
                a: Ой! Не вышло. Давай попробуем снова
                a: Не то? Сейчас точно получится, давай попробуем ещё раз
                a: Где-то ошиблись! Бывает. Давай снова
            go!: /Cinema/SuggestMovie
            
    state: Price
    # сколько стоят 2 билета на пленницу?
    # билет на джентельменов сколько стоит
    # хочу купить билеты на кавказскую пленницу, сколько стоят?
    # какая цена билета на джентельменов?
    # кавказская пленница какая цена билета?
    # билет на пленницу в какую цену?
        q!: * {сколько (стоит/стоят) * [@duckling.number] * (~билет) [на] ($GentlemenOfFortune/$CaucasianPrisoner)} *
        q!: * {[какая/какова/какую] (~цена/~стоимость) * [@duckling.number] (~билет) [на] ($GentlemenOfFortune/$CaucasianPrisoner)} *
        script:
            $session.Movie = GetMovieName($parseTree)
            $session.Price = GetMoviePrice($parseTree)
            if (typeof $parseTree["_duckling.number"] != "undefined") {
                $session.TicketsNumber = $parseTree["_duckling.number"];
                var fullprice = $session.TicketsNumber * $session.Price;
                var answer = "Один билет на фильм \"" + $session.Movie + "\" стоит " + $session.Price + ", а " + $session.TicketsNumber + " " + $nlp.conform("билет", $session.TicketsNumber) + " - " + fullprice;
            } else {
                var answer = "Один билет на фильм \"" + $session.Movie + "\" стоит " + $session.Price;
                $session.TicketsNumber = 1;
            }
            $reactions.answer(answer);
        random:
            a: Пойдешь смотреть?
            a: Берешь билеты?
        buttons:
            "Да"
            "Нет"
            
        state: GoodPrice
            q: * (да/ес/ага/агась/правильно/именно) *
            go!: /Cinema/Check
        
        state: BadPrice
            q: * (не*) *
            random:
                a: Есть и другие варианты!
                a: Давай посмотрим, что ещё можно посмотреть в кино
            go!: /Cinema/SuggestMovie
            
    state: InstantPurchase
    # 2 билета на пленницу
    # два билета на джентельменов
    # сто билетов на фильм "Кавказская пленница"
    # хочу купить два билета на "Джентельменов удачи"
        q!: * {[~хотеть/~желать/как/нужно] * $Buy @duckling.number ~билет на ($GentlemenOfFortune/$CaucasianPrisoner)} *
        script:
            $session.Movie = GetMovieName($parseTree)
            $session.TicketsNumber = $parseTree["_duckling.number"]
            $session.MoviePrice = GetMoviePrice($parseTree)
        go!: /Cinema/Check
        
    state: SuggestMovie
        q!: * {[~хотеть/~желать/как/нужно] * ($Buy) * ~билет} *
        random:
            a: На какой фильм хочешь сходить?
            a: Какой фильм хочешь посмотреть?
            a: Какой фильм хочешь глянуть?
        buttons:
            "Джентельмены удачи"
            "Кавказская пленница"
            
        state: ChooseMovie
            q: * ($GentlemenOfFortune/$CaucasianPrisoner) *
            script:
                $session.Movie = GetMovieName($parseTree)
                $session.Price = GetMoviePrice($parseTree)
            go!: /Cinema/HowManyTickets
            
        state: LocalCatchAll || noContext = true
            event: noMatch
            a: Такого фильма в прокате нет. Выбери, пожалуйста, фильм из списка
            go!: ..
            
    state: HowManyTickets || modal = true
        q!: * {[~хотеть/~желать/как/нужно] * $Buy * ~билет на ($GentlemenOfFortune/$CaucasianPrisoner)} *
        script:
            $session.Movie = GetMovieName($parseTree)
            $session.Price = GetMoviePrice($parseTree)
            var answer = "Один билет на фильм \"" + $session.Movie + "\" стоит " + $session.Price + ". Сколько билетов понадобится?";
            $reactions.answer(answer);
        
        state: GetTicketsNumber
            q: * @duckling.number *
            script:
                $session.TicketsNumber = $parseTree["_duckling.number"]
            go!: /Cinema/Check
            
        state: LocalCatchAll || noContext = true
            event: noMatch
            a: Это точно количество билетов? Напиши, пожалуйста, цифрой
            go!: ..
            
    state: Check
        script:
            var answer = $session.TicketsNumber + " " + $nlp.conform("билет", $session.TicketsNumber) + " по " + $session.MoviePrice + " на фильм \"" + $session.Movie + "\"";
            $reactions.answer(answer);
        random:
            a: Всё верно?
            a: Правильно?
        buttons:
            "Да"
            "Нет, давай заново"
            
        state: SendPayLink
            q: * (да/ес/ага/агась/правильно/именно) *
            random:
                a: Отлично! Сейчас отправлю ссылку на оплату
                a: Супер! Сейчас отправлю ссылку на оплату
                a: Здорово! Лови ссылку на оплату
            a: https://SomeLinkToPayForTheTickets
            random:
                a: Хорошего просмотра!
                a: Надеюсь, фильм тебе понравится!
                a: Хорошего похода в кинотеатр!
                
        state: StartOver
            q: * (не*) *
            random:
                a: Ой! Не вышло. Давай попробуем снова
                a: Не то? Сейчас точно получится, давай попробуем ещё раз
                a: Где-то ошиблись! Бывает. Давай снова
            go!: /Cinema/Hello
            
    state: Bye
        q!: * (пока/до свидан*/досвидан*/досвидос) *
        random:
            a: Рад был помочь! Пока!
            a: Всего хорошего!
            a: До новых встреч!
            
    state: CatchAll || noContext = true
        event!: noMatch
        random:
            a: Не совсем понял. Можешь по-другому как-то написать, пожалуйста?
            a: Не понимаю :(  Можешь, пожалуйста, переформулировать?
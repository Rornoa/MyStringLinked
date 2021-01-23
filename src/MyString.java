public class MyString {
    private Item head; //обьявлем указатель на начало списка блоков

    private MyString(String string) { //Мы создаем кастомную строку с символами из поданой строки
        int steps = string.length() / Item.SIZE_ITEM;  //steps - колличество заполненных подряд блоков
        Item currentItem = new Item(string.substring(steps * Item.SIZE_ITEM), null); //создаем currentItem - последний, не полностью заполненный блок
        for (int i = steps - 1; i >= 0; i--) //Начиная с последнего полностью заполненного блока, i - номер текущего блока
            currentItem = new Item(string.substring(i * Item.SIZE_ITEM, (i + 1) * Item.SIZE_ITEM), currentItem);//заполнение блоков начиная с неполного и заканчивая на первом блоке слева
        head = currentItem; //присваеваем первому блоку слева указатель на начало списка блоков
    }

    private MyString(Item head) {
        this.head = head; //присвоение head'у поданного параметра
    }

    private MyString(MyString myString){     //Конструктор для копий MyString
        Item clone=new Item(myString.head);  //clone - это копия head
        this.head=clone;  //присвоение head'у нового объекта (новый mystring) клона head'a поданного параметра(который myString)
        Item currentItem=myString.head.next;  // новой ссылке currentItem присваивается ссылка на второй блок поданного параметра (НЕ КЛОНА,А ОРИГИНАЛА)
        while(currentItem!=null){
            Item t=clone;        //запоминаем в ссылку t старое значение,на которое указывает ссылка clone(потому что в следующей строке ей присвоится новое значение)
            clone=new Item(currentItem); //присвоение ссылке clone копии текущего блока
            t.next=clone;               //присвоение параметру next блока, на который указывает t(копия предыдущего блока) новой копии, то есть копии текущего блока
            currentItem=currentItem.next; //переход к следующему блоку
        }
        //     this(myString.toString());//но можно создать копию параметра, вызвав конструктор,принимающий строку, и передав в него строчное представления этого параметра
    }

    static class Item {
        char[] symbols;
        byte size;
        final static int SIZE_ITEM = 16;
        Item next;

        Item(String s, Item n) { //Конструктор блока
            symbols = s.toCharArray();
            size = (byte) symbols.length;
            next = n;
        }

        Item(Item item) {                        //Конструктор копии блоков
            this.size = item.size;
            this.symbols=new char[16];
            MyString.copyArray(item.symbols, 0, size, this.symbols, 0);
            this.next = null;
        }

        Item splitItem(int index) {  //Метод разделения блока на два, используется в insert
            char[] chars = new char[16];
            for (int i = index; i < this.size; i++) {
                chars[i - index] = this.symbols[i];
                this.symbols[i] = 0;
            }
            return new Item(new String(chars), null);
        }
    }

    private static void copyArray(char[] first, int indexFirst, int length, char[] second, int indexSecond) { //метод копирования массива
        for (int i = indexFirst; i < indexFirst + length; i++)
            second[indexSecond++] = first[i];//каждый раз обращаемся к следующему элементу массива (поэтому и ++)
    }

    @Override
    public String toString() { //Из нашей кастомной строки нужно вернуть обычную String, переопределим этот метод
        int charIndex = 0;
        Item currentItem = head;
        char[] chars = new char[length()];
        while (currentItem != null) {
            for (int i = 0; i < length(); i++)
                chars[charIndex++] = currentItem.symbols[i];
            currentItem = currentItem.next;
        }
        return new String(chars);
    }

    private void collab() {  //Метод слияния
        Item currentItem = head;
        char[] chars = new char[length()];
        int index = 0;
        while (currentItem != null) {
            copyArray(currentItem.symbols, 0, currentItem.size, chars, index += currentItem.size);
            currentItem = currentItem.next;
        }
        this.head=new MyString(new String(chars)).head;
    }

    private int length() {
        int b = 0;
        Item a = head;

        while (a != null) {
            b = b + a.size;
            a = a.next;
        }
        return b;
    }

    static class Position {
        int indexInItem; //Номер символа в блоке
        Item item; //блок где находится искомый символ
        Position(Item currentItem, int indexInItem) {
            this.indexInItem = indexInItem;
            this.item = currentItem;
        }
    }

    private Position getPosition(int index) {                //Метод возвращающий позицию символа
        if (index>=length()) throw new IndexOutOfBoundsException("Подано неверное значение в переменную index");

        Item currentItem = head;
        while (index >= currentItem.size) {
            index -= currentItem.size;
            currentItem = currentItem.next;
        }
        return new Position(currentItem, index);
    }

    private char charAt(int index) {                         //Метод charAt()
        if (index>=length()) throw new IndexOutOfBoundsException("Подано неверное значение в переменную index");

        Position position = getPosition(index);
        return position.item.symbols[position.indexInItem];
    }

    void setCharAt(int index, char symbol) {                //Метод setCharAt()
        if (index>=length()) throw new IndexOutOfBoundsException("Подано неверное значение в переменную index");

        Position position = getPosition(index);
        position.item.symbols[position.indexInItem] = symbol;
    }

    MyString substring(int start, int end) {
        if (start > end || end >= length())
            throw new IndexOutOfBoundsException("Поданы неверные значения начала и конца для выделения подстроки");

        Position positionStart = getPosition(start);
        Position positionEnd = getPosition(end);
        if (positionStart.item == positionEnd.item) { //Если выделяемая подстрока состоит из единственного блока
            char[] chars = new char[16];
            copyArray(positionStart.item.symbols, start, end - start, chars, 0);
            return new MyString(new String(chars));
        }
        Item cloneStart = new Item("",null);//создаем пустой блок, куда скопируем символы из блока, где находится начало подстроки
        copyArray(positionStart.item.symbols, positionStart.indexInItem, positionStart.item.size - positionStart.indexInItem, cloneStart.symbols, 0);//копируем символы из блока, содержащего начало подстроки, начиная сл стартового до конца, в новый созданный выше блок
        Item cloneEnd = new Item(positionEnd.item);//создаем копию блока, содержащего конец подстроки
        for (int i = positionEnd.indexInItem + 1; i < Item.SIZE_ITEM; i++) cloneEnd.symbols[i] = 0;// удаляем из копии символы, идущие после конца (end)
        Item lastItem = cloneStart;// инициализация ссылки, которая в цикле будет означать последний клон для создания ему связи. К началу цикла она ссылается на клон стартового блока, то есть блока, содержащего начало подстроки
        Item currentItem = positionStart.item.next; //инициализация ссылки, которая в цикле будет значить текущий блок оригинальной строки. к началу цикла она ссылается на следующий блок после стартового
        while (currentItem != positionEnd.item) { //цикл, повторяющийся до тех пор, пока текущий элемент не станет последним
            lastItem.next = new Item(currentItem); //присвоение next'у последнего клона, клона текущего элемента
            lastItem = lastItem.next;       // переход на следующий клон
            currentItem = currentItem.next;//переход на следующий блок оригинальной строки
        }
        lastItem.next = cloneEnd; //присвоение next'у копии предпоследнего блока, копии последнего блока
        return new MyString(cloneStart);
    }

    private void append(MyString myString) {
        collab();
        Item item = this.getPosition(length()-1).item;//находим нужный блок и позицию для вставки
        MyString copy = new MyString(myString.toString()); //содаем новую кастомную строку для того чтобы присоеденить ее в конец списка блоков
        item.next = copy.head;
    }

    void append(String string) {
        append(new MyString(string));
    }

    void append(char ch) {
        collab();
        Item currentItem = this.getPosition(length()-1).item; //Находим нужную блок и запоминаем ее в currentItem
        if (currentItem.size < Item.SIZE_ITEM) currentItem.symbols[currentItem.size] = ch; //присоединяем массив ch в конец если блок заполнен не полностью
        else currentItem.next = new Item(ch + "", null); //создаем новый блок и заполняем есго массивом ch если блок заполнен полностью
    }


    private void insert(int index,MyString myString)  //Метод вставки строки в кастомную строку
    {
        if (index >= length()) throw new IndexOutOfBoundsException("Подано неверное значение в переменную index");

        MyString stringForInsert = new MyString(myString);  //создаем новую кастомную строку stringFOrInsert, которую будем вставлять в исходную
        Position position = getPosition(index);     //Находим позицию куда нужно вставить подстроку и запоминаем ее в position.
        Item cloneBlock = position.item.splitItem(position.indexInItem);      //Мы создаем отдельный блок и помещаем этот самый массив в этот самый блок
        Item last = position.item.next;  //Запоминаем в last блок, которые присоеденим в последнюю очередь (за ним могут следовать оставшиеся блоки)
        position.item.next = stringForInsert.head; //мы связываем нашу stringForInsert с блоком в которую вставляем
        Item currentItem = stringForInsert.head; // в currentItem запоминаем первый блок вставляемой строки
        while (currentItem.next != null) currentItem = currentItem.next; //циклом проходимся по блокам вставляемой строки, пока currentItem не будет содержать ссылку на последний блок вставляемой строки
        currentItem.next = cloneBlock; //и присоединяем блок с оставшимися символами блока в который мы вставляли, к последнему блоку вставлямой строки
        cloneBlock.next = last; //присоединяем оставшиеся блоки
    }

    void insert(int index, String str) {
        insert(index, new MyString(str));
    }
}






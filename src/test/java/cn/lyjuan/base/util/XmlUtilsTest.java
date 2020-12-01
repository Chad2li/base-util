package cn.lyjuan.base.util;

import junit.framework.TestCase;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class XmlUtilsTest {

    @Test
    public void genXmlList(){
        List<XmlAddress> list = new ArrayList<>(2);
        String xml = XmlUtils.genXml(list);
        System.out.println(xml);

        XmlAddress ad1 = new XmlAddress();
        ad1.setProvince("浙江");
        ad1.setCity("杭州");
        ad1.setArea("上城区");
        ad1.setAddress("某街XXX号");
        list.add(ad1);
        XmlAddress ad2 = new XmlAddress();
        ad2.setProvince("北京");
        ad2.setCity("北京");
        ad2.setArea("朝阳区");
        ad2.setCity("某街XXXX号");
        list.add(ad2);
        xml = XmlUtils.genXml(list);
        System.out.println(xml);
    }

    @Test
    public void genXml(){
        XmlModel model = new XmlModel();
        model.setName("Zhangsan");
        model.setAge(18);
        model.setGender(Byte.parseByte("1"));
        model.setScore(99.9);
        model.setBirthday(DateUtils.parseDate("1988-01-01", "yyyy-MM-dd"));
        model.setCreatetime(LocalDateTime.now());

        Map<String, String> map = new HashMap<>(2);
        map.put("firend1", "Wanger");
        map.put("firend2", "Lisi");
        model.setMap(map);

        List<XmlAddress> ads = new ArrayList<>(2);
        XmlAddress ad1 = new XmlAddress();
        ad1.setProvince("浙江");
        ad1.setCity("杭州");
        ad1.setArea("上城区");
        ad1.setAddress("某街XXX号");
        ads.add(ad1);
        XmlAddress ad2 = new XmlAddress();
        ad2.setProvince("北京");
        ad2.setCity("北京");
        ad2.setArea("朝阳区");
        ad2.setCity("某街XXXX号");
        ads.add(ad2);
        model.setAddresses(ads);

        Set<Mail> mails = new HashSet<>(2);
        Mail mail1 = new Mail();
        mail1.setMail("11111@mail.com");
        mail1.setName("11111");
        mails.add(mail1);
        Mail mail2 = new Mail();
        mail2.setMail("22222@mail.com");
        mail2.setName("22222");
        mails.add(mail2);
        model.setMails(mails);

        String xml = XmlUtils.genXml(model);
        System.out.println(xml);
    }


    public class XmlAddress{
        private String province;
        private String city;
        private String area;
        private String address;

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getArea() {
            return area;
        }

        public void setArea(String area) {
            this.area = area;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }
    public class Mail{
        private String name;
        private String mail;

        public String getMail() {
            return mail;
        }

        public void setMail(String mail) {
            this.mail = mail;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    public class XmlModel{
        private Map<String, String> map;
        @XmlUtils.XmlItem("addressItem")
        private List<XmlAddress> addresses;
        @XmlUtils.XmlItem("mailItem")
        private Set<Mail> mails;
        private String name;
        private int age;
        private byte gender;
        private double score;
        private LocalDate birthday;
        private LocalDateTime createtime;

        public Map<String, String> getMap() {
            return map;
        }

        public void setMap(Map<String, String> map) {
            this.map = map;
        }

        public List<XmlAddress> getAddresses() {
            return addresses;
        }

        public void setAddresses(List<XmlAddress> addresses) {
            this.addresses = addresses;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public byte getGender() {
            return gender;
        }

        public void setGender(byte gender) {
            this.gender = gender;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public LocalDate getBirthday() {
            return birthday;
        }

        public void setBirthday(LocalDate birthday) {
            this.birthday = birthday;
        }

        public Set<Mail> getMails() {
            return mails;
        }

        public void setMails(Set<Mail> mails) {
            this.mails = mails;
        }

        public LocalDateTime getCreatetime() {
            return createtime;
        }

        public void setCreatetime(LocalDateTime createtime) {
            this.createtime = createtime;
        }
    }
}
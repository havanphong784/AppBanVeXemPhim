package com.cinema.datastructure;

import com.cinema.model.Showtime;
import java.time.LocalDate;

public class ShowtimeLinkedList {

    public static class YearNode {
        public int yearValue;
        public YearNode next;
        public MonthNode headMonth;

        public YearNode(int yearValue) {
            this.yearValue = yearValue;
            this.next = null;
            this.headMonth = null;
        }
    }

    public static class MonthNode {
        public int monthValue;
        public MonthNode next;
        public DayNode headDay;

        public MonthNode(int monthValue) {
            this.monthValue = monthValue;
            this.next = null;
            this.headDay = null;
        }
    }

    public static class DayNode {
        public int dayValue;
        public DayNode next;
        public ShowtimeNode headShowtime;

        public DayNode(int dayValue) {
            this.dayValue = dayValue;
            this.next = null;
            this.headShowtime = null;
        }
    }

    public static class ShowtimeNode {
        public Showtime showtime;
        public ShowtimeNode next;

        public ShowtimeNode(Showtime showtime) {
            this.showtime = showtime;
            this.next = null;
        }
    }

    public static class StatsResult {
        public int count;
        public double sumRevenue;
        public double avgRevenue;
        public double maxRevenue;
        public double minRevenue;

        public StatsResult(int count, double sumRevenue, double avgRevenue, double maxRevenue, double minRevenue) {
            this.count = count;
            this.sumRevenue = sumRevenue;
            this.avgRevenue = avgRevenue;
            this.maxRevenue = maxRevenue;
            this.minRevenue = minRevenue;
        }

        @Override
        public String toString() {
            return String.format("Tổng: %.2f VNĐ, Trung bình: %.2f VNĐ, Lớn nhất: %.2f VNĐ, Nhỏ nhất: %.2f VNĐ, Số suất chiếu: %d",
                    sumRevenue, avgRevenue, maxRevenue, minRevenue, count);
        }
    }

    private YearNode headYear;
    private int size;

    public ShowtimeLinkedList() {
        this.headYear = null;
        this.size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        headYear = null;
        size = 0;
    }

    // Insert showtime in hierarchical sorted order
    public void insert(Showtime showtime) {
        if (showtime == null || showtime.getShowDate() == null) return;

        LocalDate localDate = showtime.getShowDate().toLocalDate();
        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int day = localDate.getDayOfMonth();

        // 1. Insert Year
        YearNode yearNode = findOrInsertYear(year);

        // 2. Insert Month under Year
        MonthNode monthNode = findOrInsertMonth(yearNode, month);

        // 3. Insert Day under Month
        DayNode dayNode = findOrInsertDay(monthNode, day);

        // 4. Insert Showtime under Day (sorted by Time)
        insertShowtime(dayNode, showtime);

        size++;
    }

    private YearNode findOrInsertYear(int year) {
        if (headYear == null || headYear.yearValue > year) {
            YearNode newNode = new YearNode(year);
            newNode.next = headYear;
            headYear = newNode;
            return newNode;
        }

        YearNode current = headYear;
        while (current != null) {
            if (current.yearValue == year) {
                return current;
            }
            if (current.next == null || current.next.yearValue > year) {
                YearNode newNode = new YearNode(year);
                newNode.next = current.next;
                current.next = newNode;
                return newNode;
            }
            current = current.next;
        }
        return null;
    }

    private MonthNode findOrInsertMonth(YearNode yearNode, int month) {
        if (yearNode.headMonth == null || yearNode.headMonth.monthValue > month) {
            MonthNode newNode = new MonthNode(month);
            newNode.next = yearNode.headMonth;
            yearNode.headMonth = newNode;
            return newNode;
        }

        MonthNode current = yearNode.headMonth;
        while (current != null) {
            if (current.monthValue == month) {
                return current;
            }
            if (current.next == null || current.next.monthValue > month) {
                MonthNode newNode = new MonthNode(month);
                newNode.next = current.next;
                current.next = newNode;
                return newNode;
            }
            current = current.next;
        }
        return null;
    }

    private DayNode findOrInsertDay(MonthNode monthNode, int day) {
        if (monthNode.headDay == null || monthNode.headDay.dayValue > day) {
            DayNode newNode = new DayNode(day);
            newNode.next = monthNode.headDay;
            monthNode.headDay = newNode;
            return newNode;
        }

        DayNode current = monthNode.headDay;
        while (current != null) {
            if (current.dayValue == day) {
                return current;
            }
            if (current.next == null || current.next.dayValue > day) {
                DayNode newNode = new DayNode(day);
                newNode.next = current.next;
                current.next = newNode;
                return newNode;
            }
            current = current.next;
        }
        return null;
    }

    private void insertShowtime(DayNode dayNode, Showtime showtime) {
        ShowtimeNode newNode = new ShowtimeNode(showtime);
        if (dayNode.headShowtime == null || dayNode.headShowtime.showtime.getShowTime().compareTo(showtime.getShowTime()) > 0) {
            newNode.next = dayNode.headShowtime;
            dayNode.headShowtime = newNode;
            return;
        }

        ShowtimeNode current = dayNode.headShowtime;
        while (current != null) {
            if (current.next == null || current.next.showtime.getShowTime().compareTo(showtime.getShowTime()) > 0) {
                newNode.next = current.next;
                current.next = newNode;
                return;
            }
            current = current.next;
        }
    }

    // Delete showtime by ID
    public boolean delete(int showtimeId) {
        if (headYear == null) return false;

        YearNode prevYear = null;
        YearNode currYear = headYear;

        while (currYear != null) {
            MonthNode prevMonth = null;
            MonthNode currMonth = currYear.headMonth;

            while (currMonth != null) {
                DayNode prevDay = null;
                DayNode currDay = currMonth.headDay;

                while (currDay != null) {
                    ShowtimeNode prevShow = null;
                    ShowtimeNode currShow = currDay.headShowtime;

                    while (currShow != null) {
                        if (currShow.showtime.getId() == showtimeId) {
                            // Remove ShowtimeNode
                            if (prevShow == null) {
                                currDay.headShowtime = currShow.next;
                            } else {
                                prevShow.next = currShow.next;
                            }
                            size--;

                            // Clean up empty DayNode
                            if (currDay.headShowtime == null) {
                                if (prevDay == null) {
                                    currMonth.headDay = currDay.next;
                                } else {
                                    prevDay.next = currDay.next;
                                }
                            }

                            // Clean up empty MonthNode
                            if (currMonth.headDay == null) {
                                if (prevMonth == null) {
                                    currYear.headMonth = currMonth.next;
                                } else {
                                    prevMonth.next = currMonth.next;
                                }
                            }

                            // Clean up empty YearNode
                            if (currYear.headMonth == null) {
                                if (prevYear == null) {
                                    headYear = currYear.next;
                                } else {
                                    prevYear.next = currYear.next;
                                }
                            }

                            return true;
                        }
                        prevShow = currShow;
                        currShow = currShow.next;
                    }

                    if (currDay.headShowtime != null) {
                        prevDay = currDay;
                    }
                    currDay = currDay.next;
                }

                if (currMonth.headDay != null) {
                    prevMonth = currMonth;
                }
                currMonth = currMonth.next;
            }

            if (currYear.headMonth != null) {
                prevYear = currYear;
            }
            currYear = currYear.next;
        }

        return false;
    }

    // Search by Date (Year, Month, Day)
    public MyList<Showtime> searchByDate(int year, int month, int day) {
        MyList<Showtime> result = new MyList<>();

        YearNode y = headYear;
        while (y != null && y.yearValue != year) {
            y = y.next;
        }
        if (y == null) return result;

        MonthNode m = y.headMonth;
        while (m != null && m.monthValue != month) {
            m = m.next;
        }
        if (m == null) return result;

        DayNode d = m.headDay;
        while (d != null && d.dayValue != day) {
            d = d.next;
        }
        if (d == null) return result;

        ShowtimeNode s = d.headShowtime;
        while (s != null) {
            result.add(s.showtime);
            s = s.next;
        }

        return result;
    }

    // Get all showtimes in order
    public MyList<Showtime> getAllShowtimes() {
        MyList<Showtime> result = new MyList<>();
        YearNode y = headYear;
        while (y != null) {
            MonthNode m = y.headMonth;
            while (m != null) {
                DayNode d = m.headDay;
                while (d != null) {
                    ShowtimeNode s = d.headShowtime;
                    while (s != null) {
                        result.add(s.showtime);
                        s = s.next;
                    }
                    d = d.next;
                }
                m = m.next;
            }
            y = y.next;
        }
        return result;
    }

    // ----------------------------------------------------
    // Statistics Calculations
    // ----------------------------------------------------

    public StatsResult getStatsForDay(int dayValue, MyList<Showtime> outShowtimes) {
        int count = 0;
        double sum = 0;
        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;

        YearNode y = headYear;
        while (y != null) {
            MonthNode m = y.headMonth;
            while (m != null) {
                DayNode d = m.headDay;
                while (d != null) {
                    if (d.dayValue == dayValue) {
                        ShowtimeNode s = d.headShowtime;
                        while (s != null) {
                            outShowtimes.add(s.showtime);
                            count++;
                            double rev = s.showtime.getRevenue();
                            sum += rev;
                            if (rev > max) max = rev;
                            if (rev < min) min = rev;
                            s = s.next;
                        }
                    }
                    d = d.next;
                }
                m = m.next;
            }
            y = y.next;
        }

        if (count == 0) {
            return new StatsResult(0, 0, 0, 0, 0);
        }
        return new StatsResult(count, sum, sum / count, max, min);
    }

    public StatsResult getStatsForMonth(int monthValue, MyList<Showtime> outShowtimes) {
        int count = 0;
        double sum = 0;
        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;

        YearNode y = headYear;
        while (y != null) {
            MonthNode m = y.headMonth;
            while (m != null) {
                if (m.monthValue == monthValue) {
                    DayNode d = m.headDay;
                    while (d != null) {
                        ShowtimeNode s = d.headShowtime;
                        while (s != null) {
                            outShowtimes.add(s.showtime);
                            count++;
                            double rev = s.showtime.getRevenue();
                            sum += rev;
                            if (rev > max) max = rev;
                            if (rev < min) min = rev;
                            s = s.next;
                        }
                        d = d.next;
                    }
                }
                m = m.next;
            }
            y = y.next;
        }

        if (count == 0) {
            return new StatsResult(0, 0, 0, 0, 0);
        }
        return new StatsResult(count, sum, sum / count, max, min);
    }

    public StatsResult getStatsForYear(int yearValue, MyList<Showtime> outShowtimes) {
        int count = 0;
        double sum = 0;
        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;

        YearNode y = headYear;
        while (y != null) {
            if (y.yearValue == yearValue) {
                MonthNode m = y.headMonth;
                while (m != null) {
                    DayNode d = m.headDay;
                    while (d != null) {
                        ShowtimeNode s = d.headShowtime;
                        while (s != null) {
                            outShowtimes.add(s.showtime);
                            count++;
                            double rev = s.showtime.getRevenue();
                            sum += rev;
                            if (rev > max) max = rev;
                            if (rev < min) min = rev;
                            s = s.next;
                        }
                        d = d.next;
                    }
                    m = m.next;
                }
            }
            y = y.next;
        }

        if (count == 0) {
            return new StatsResult(0, 0, 0, 0, 0);
        }
        return new StatsResult(count, sum, sum / count, max, min);
    }

    public StatsResult getStatsAll(MyList<Showtime> outShowtimes) {
        int count = 0;
        double sum = 0;
        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;

        YearNode y = headYear;
        while (y != null) {
            MonthNode m = y.headMonth;
            while (m != null) {
                DayNode d = m.headDay;
                while (d != null) {
                    ShowtimeNode s = d.headShowtime;
                    while (s != null) {
                        outShowtimes.add(s.showtime);
                        count++;
                        double rev = s.showtime.getRevenue();
                        sum += rev;
                        if (rev > max) max = rev;
                        if (rev < min) min = rev;
                        s = s.next;
                    }
                    d = d.next;
                }
                m = m.next;
            }
            y = y.next;
        }

        if (count == 0) {
            return new StatsResult(0, 0, 0, 0, 0);
        }
        return new StatsResult(count, sum, sum / count, max, min);
    }
}

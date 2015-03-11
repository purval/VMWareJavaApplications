package edu.sjsu.vmware.executors;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Test {
	 public static boolean ASC = true;
	 public static boolean DESC = false;
	public static void main(String[] args) {
		double baseCPU = 4064.0;
		double val = 2641.0;
		double perc = (val/baseCPU)*100;
		System.out.println(perc);
		
        /*// Creating dummy unsorted map
        Map<String, Long> unsortMap = new HashMap<String, Long>();
        unsortMap.put("B", 55l);
        unsortMap.put("A", 80l);
        unsortMap.put("D", 20l);
        unsortMap.put("C", 70l);

        System.out.println("Before sorting......");
        printMap(unsortMap);

        System.out.println("After sorting ascending order......");
        Map<String, Long> sortedMapAsc = sortByComparator(unsortMap, ASC);
        printMap(sortedMapAsc);


        System.out.println("After sorting descindeng order......");
        Map<String, Long> sortedMapDesc = sortByComparator(unsortMap, DESC);
        printMap(sortedMapDesc);*/

    }

    private static Map<String, Long> sortByComparator(Map<String, Long> unsortMap, final boolean order)
    {

        List<Entry<String, Long>> list = new LinkedList<Entry<String, Long>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Long>>()
        {
            public int compare(Entry<String, Long> o1,
                    Entry<String, Long> o2)
            {
                if (order)
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else
                {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Long> sortedMap = new LinkedHashMap<String, Long>();
        for (Entry<String, Long> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public static void printMap(Map<String, Long> map)
    {
        for (Entry<String, Long> entry : map.entrySet())
        {
            System.out.println("Key : " + entry.getKey() + " Value : "+ entry.getValue());
        }
    }
}

package com.example.DiplomaGeneration.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GetDataHTML {
    public int pageSize;
    public List<Integer> place = new ArrayList<>();
    public List<String> teams = new ArrayList<>();
    public List<Integer> tasks = new ArrayList<>();

    private Document getPage(String url) {
        Document document = null;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return document;
    }

    private Document getFileUrl(MultipartFile inputFile) {
        Document document = null;
        try {
            document = Jsoup.parse(inputFile.getInputStream(), "UTF-8", "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return document;
    }

    public Elements mergeElements(Elements elements1, Elements elements2) {
        Elements mergedElements = new Elements();
        int size1 = elements1.size(), size2 = elements2.size();
        int maxLength = Math.max(size1, size2);
        for (int i = 0; i < maxLength; i++) {
            if (i < size1) {
                mergedElements.add(elements1.get(i));
            }
            if (i < size2) {
                mergedElements.add(elements2.get(i));
            }
        }
        return mergedElements;
    }

    public void parsingTable1(Element table) {
        Elements rows = table.select("tr");
        for (Element row : rows) {
            Elements cells = row.select("td");
            if (cells.size() >= 3) {
                int participantNumber = 0;
                String participantStr = cells.get(0).text().trim();
                if (!participantStr.isEmpty()) {
                    participantNumber = Integer.parseInt(cells.get(0).text().trim());
                }

                String teamInfo = cells.get(1).text().trim();
                String teamName = extractTeamName(teamInfo);
                int someNumber = 0;
                if (!participantStr.isEmpty()) {
                    someNumber = Integer.parseInt(cells.get(2).text().trim());
                }
                place.add(participantNumber);
                teams.add(teamName);
                tasks.add(someNumber);
            }
        }

    }


    private static String extractTeamName(String teamInfo) {
        System.out.println("teamInfo=" + teamInfo);
        String[] parts = teamInfo.split(":");
        if (parts.length > 1) {
            String teamName = parts[1].trim();
            if (teamName.contains("(")) {
                int indexOfBracket = teamName.lastIndexOf('(');
                teamName = teamName.substring(0, indexOfBracket).trim();
            } else {
                teamName = parts[0].trim();
            }
            return teamName;
        }
        return teamInfo.trim();
    }

    private void parsingTable2(Element table) {
        Elements rows = table.select("tbody tr");
        pageSize = rows.size();
        for (Element row : rows) {
            Elements cells = row.select("td");
            if (cells.size() >= 3) {
                String teamName = cells.get(1).ownText().trim();
                String taskValue = cells.get(2).ownText().trim();
                if (taskValue.isEmpty()) {
                    taskValue = cells.get(2).html().split("<br>")[0].trim();
                }
                if (teamName.isEmpty()) {
                    teamName = cells.get(1).html().split("<br>")[0].trim();
                }
                teams.add(teamName);
                place.add(teams.size());
                try {
                    tasks.add(Integer.parseInt(taskValue));
                } catch (NumberFormatException e) {
                    tasks.add(0);
                }

            }
        }
    }

    public String getData(String url, MultipartFile file) {
        Document page = null;
        if (!url.isEmpty()) {
            page = getPage(url);
        } else if (!file.isEmpty()) {
            page = getFileUrl(file);
        }
        if (page == null) {
            return "Не удалось загрузить страницу: " + url;
        }
        Element table = page.select("table[class=standings]").first();
        Element table2 = page.select("table.table.table-hover.table-bordered.table-responsive").first();
        if (table != null) {
            parsingTable1(table);
        } else if (table2 != null) {
            parsingTable2(table2);
        } else {
            return "Таблица результатов не найдена";
        }
        System.out.println("Teams: ");
        System.out.println(teams);
        return null;
    }
}

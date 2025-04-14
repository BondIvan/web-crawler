ALTER TABLE crawler.news_article
    MODIFY COLUMN publish_date DATE DEFAULT (CURRENT_DATE);
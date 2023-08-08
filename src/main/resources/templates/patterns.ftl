<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Pattern Search</title>
    <link rel="stylesheet" href="/static/main.css">
</head>
<body>
<h1>Patterns:</h1>

<div class="pattern-search-container">
    <section class="pattern-search">
        <p>Search:</p>
    </section>
    <section class="pattern-search-result">
        <p>Results:</p>
        <#list patterns as pattern>
        <div class="patterns">
            <a href="/pattern/${pattern.id}" class="pattern-name">
                ${pattern.name}
            </a>
            <a href="" class="pattern-publisher">
                ${pattern.publisher}
            </a>
        </div>
        </#list>
    </section>
</div>
</body>
</html>

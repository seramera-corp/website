<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Look at my projects!</title>
    <link rel="stylesheet" href="/static/main.css">
</head>
<body>
<h1>Hello,</h1>
<h1>I am ${user.name} and these are my beautiful projects:</h1>
<div class="user-projects">
    <#list projects as project>
        <div class="project">
            <img src="${project.imgUrl}" alt="" class="project-img">
            <a href="/project/${project.id}" class="project-name">
                ${project.name}
            </a>
        </div>
    <#else>
        No projects found for this user yet. Start sewing now!
    </#list>
</div>
</body>
</html>

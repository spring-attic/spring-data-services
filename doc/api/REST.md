# REST API

### Resources

Here's what some sample resources would look like on the server side as a JPA entity:

    @Entity
    class Person {
      @Id @GeneratedValue Long id;
      String name;
      String type;
      @OneToMany
      List<Account> accounts;
    }

    @Entity
    class Account {
      @Id @GeneratedValue Long id;
      String type;
      String url;
    }

### Discoverability

Following the principles of [HATEOS](http://en.wikipedia.org/wiki/HATEOAS), the REST API should be fundamentally discoverable, starting with the base URL:

    curl -v http://localhost:8080/baseurl/

Without specifying an `Accept` header, the response would look like:

    HTTP/1.1 200 OK
    Date: ...
    Host: localhost:8080
    Server: Spring Data Services Web Exporter/1.0.0.BUILD-SNAPSHOT

    http://localhost:8080/baseurl/service1/
    http://localhost:8080/baseurl/service2/
    http://localhost:8080/baseurl/service3/

Specifying an Accept header should cause the output to be rendered in the representation specified. e.g. for JSON:

    curl -v -H "Accept: application/json" http://localhost:8080/baseurl/

    HTTP/1.1 200 OK
    Date: ...
    Host: localhost:8080
    X-JSON-Format-Version: 1.0
    Server: Spring Data Services Web Exporter/1.0.0.BUILD-SNAPSHOT

    [
      {"$ref": "http://localhost:8080/baseurl/service1/"},
      {"$ref": "http://localhost:8080/baseurl/service2/"},
      {"$ref": "http://localhost:8080/baseurl/service3/"}
    ]

### Links

The method for exposing links here is somewhat arbitrary since there's no real consenus on how this should be done in JSON. The DOJO framework has [some linking functionality](http://www.sitepen.com/blog/2008/06/17/json-referencing-in-dojo/) that uses "$ref" as a field name, which corresponds to what MongoDB documents use for DBRefs. This convention seems as good as any other, so is the one we'll be using here.

If the server understands links on the entity (meaning it's a JPA Entity with a @OneTo... et al mapping), then the user could POST new associations directly inline in the parent JSON:

    POST /baseurl/person HTTP/1.1
    Content-Type: application/json

    {
      "name": "John Doe",
      "type": "profile",
      "accounts": [
        {
          "type": "facebook",
          "url": "/myfbpage"
        },
        {
          "type": "twitter",
          "url": "#!/mytwitterprofile"
        }
      ]
    }

It would be assumed that the unmarshaller would correclty interpret this as a new object graph and that the Repository implementation would correclty assign IDs to the new entities and correctly associate them with the parent.

One could mix a `$ref` with data if you wanted to update the list and add a new entry:

    PUT /baseurl/person/1 HTTP/1.1
    Content-Type: application/json

    {
      "accounts": [
        {"$ref": "http://localhost:8080/baseurl/account/1"},
        {
          "type": "twitter",
          "url": "#!/mytwitterprofile"
        }
      ]
    }

If you want to resolve the links and retrieve the actual data of the linked entities, then you'd specify a URL query parameter(s) corresponding to the property name (the parameter name is configurable so could be "properties", "property", or "bob"):

    curl -v -H "Accept: application/json" http://localhost:8080/baseurl/person/1?p=accounts

    HTTP/1.1 200 OK
    Date: ...
    Host: localhost:8080
    X-JSON-Format-Version: 1.0
    Server: Spring Data Services Web Exporter/1.0.0.BUILD-SNAPSHOT

    [
      {
        "id": 1,
        "type": "facebook",
        "url": "/myfbpage"
      },
      {
        "id": 2,
        "type": "twitter",
        "url": "#!/mytwitterprofile"
      }
    ]

### API Commands

The following table lists the possible URLs, the methods exposed on those URLs, sample input, and sample output.

<table width="100%" style="font-family: Courier, 'Courier New', fixed;">
<thead>
<tr>
  <th>GET</th>
  <th>PUT</th>
  <th>POST</th>
  <th>DELETE</th>
  <th width="20%">URL</th>
  <th width="30%">INPUT</th>
  <th width="30%">OUTPUT</th>
</tr>
</thead>
<tbody>
<tr>
  <td style="text-align: center">X</td>
  <td style="text-align: center"></td>
  <td style="text-align: center"></td>
  <td style="text-align: center"></td>
  <td>/baseurl</td>
  <td></td>
  <td>GET:<pre>
[
  {"$ref": "http://localhost:8080/baseurl/account/"},
  {"$ref": "http://localhost:8080/baseurl/person/"}
]</pre></td>
</tr>
<tr>
  <td style="text-align: center">X</td>
  <td style="text-align: center"></td>
  <td style="text-align: center">X</td>
  <td style="text-align: center"></td>
  <td>/baseurl/person</td>
  <td>POST:<pre>
{
  "name": "John Doe",
  "type": "profile"
  "accounts": [
    {"$ref": "http://localhost:8080/baseurl/account/1"}
  ]
}</pre></td>
  <td>GET:<pre>
HTTP/1.1 200 OK
Content-Type: application/json

[
  {"$ref": "http://localhost:8080/baseurl/person/1"},
  {"$ref": "http://localhost:8080/baseurl/person/2"},
  {"$ref": "http://localhost:8080/baseurl/person/3"}
]</pre>

POST:<pre>
HTTP/1.1 201 Created
Location: http://localhost:8080/baseurl/person/1

</pre></td>
</tr>
<tr>
  <td style="text-align: center">X</td>
  <td style="text-align: center">X</td>
  <td style="text-align: center"></td>
  <td style="text-align: center">X</td>
  <td>/baseurl/person/1</td>
  <td>PUT:<pre>
{
  "name": "John Doe",
  "type": "profile",
  "accounts": [
    {"$ref": "http://localhost:8080/baseurl/account/1"},
    {
      "type": "twitter",
      "url": "#!/mytwitterprofile"
    }
  ]
}</pre></td>
  <td>GET:<pre>
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 1,
  "name": "John Doe",
  "type": "profile",
  "accounts": [
    {"$ref": "http://localhost:8080/baseurl/account/1"},
    {"$ref": "http://localhost:8080/baseurl/account/2"}
  ]
}</pre>

PUT:<pre>
HTTP/1.1 204 No Content

</pre>
DELETE:<pre>
HTTP/1.1 204 No Content

</pre></td>
</tr>
<tr>
  <td style="text-align: center">X</td>
  <td style="text-align: center"></td>
  <td style="text-align: center"></td>
  <td style="text-align: center"></td>
  <td>/baseurl/person/1?p=accounts</td>
  <td></td>
  <td>GET:<pre>
HTTP/1.1 200 OK
Content-Type: application/json

[
  {
    "id": 1,
    "type": "facebook",
    "url": "/myfbpage"
  },
  {
    "id": 2,
    "type": "twitter",
    "url": "#!/mytwitterprofile"
  }
]</pre></td>
</tr>
</tbody>
var partition = function (file) {
    var article = file.trim().toLowerCase();
    var lines = article.split("\n");
    var lines = chunkify(lines, 200);
    return lines;
}

var chunkify = function (lines, n) {
    var chunks = [];
    var i = 0;
    while (i < lines.length) {
        chunks.push(lines.slice(i, i += n));
    }
    return chunks;
}

var splitWords = function (line) {  
    linestr = line.join("\n")
    var words = linestr.match(/[a-z]+/g).filter(function(str){return str.length>=2});

    //removing stop words
    var stopWords = fs.readFileSync("../stop_words.txt", "utf8").split(",");
    words = words.filter(function (word) {return stopWords.indexOf(word) <= -1});

    var initialCounts = [];
    words.forEach(function (word) {
        initialCounts.push({ word: word, count: 1 });
    });
    return initialCounts;
}

var regroup = function (pairs) {
    var maps = {};
    maps["a"] = [];
    maps["f"] = [];
    maps["k"] = [];
    maps["p"] = [];
    maps["u"] = [];

    pairs.forEach(function (pair) {
        var keyWord = pair["word"];

        if (/\b[a-e]/.test(keyWord)) {
            maps["a"].push(pair);
        }
        if (/\b[f-j]/.test(keyWord)) {
            maps["f"].push(pair);
        }
        if (/\b[k-o]/.test(keyWord)) {
            maps["k"].push(pair);
        }
        if (/\b[p-t]/.test(keyWord)) {
            maps["p"].push(pair);
        }
        if (/\b[u-z]/.test(keyWord)) {
            maps["u"].push(pair);
        }
    });
    return maps;
}

//Reduce Function
var reduceCounts = function (wordGroups) {
    return wordGroups.reduce(function (freqMap, wordtag) {
        var word = wordtag["word"];
        if (word in freqMap) {
            ++freqMap[word];
        } else {
            freqMap[word] = 1;
        }
        return freqMap;
    }, {});
}

var sort = function (freqMap) {
    var sortedArr = [];
    for (var key in freqMap) {
        sortedArr.push([key, freqMap[key]]);
    };
    sortedArr.sort(function (a, b) { return b[1] - a[1] });
    return sortedArr;
};

var top25 = function (sortedArr) {
    var result = "";
    for (var i = 0; i < 25; i++) {
        result = sortedArr[i][0] + " - " + sortedArr[i][1];
        console.log(result);
    }
};

var items = function (obj) {
    var arr = [];
    for (var i in obj) {
        arr.push(obj[i]);
    }
    return arr;
};


//Main Function
var arg = process.argv.splice(2);
var path = "../" + arg[0].toString();
var fs = require('fs');
var file = fs.readFileSync(path, "utf8");

var splits = [];
splits = splits.concat.apply(splits, partition(file).map(splitWords));
var splits_per_word = regroup(splits);

var wordMap = items(splits_per_word).map(reduceCounts);

var freqMap = {};
for (var i = 0; i < wordMap.length; i++) {
    for (var key in wordMap[i])
        freqMap[key] = wordMap[i][key];
};

top25(sort(freqMap));


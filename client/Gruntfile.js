module.exports = function (grunt) {

    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-contrib-less');
    grunt.loadNpmTasks('grunt-karma');
    grunt.loadNpmTasks('grunt-html2js');

    // Default task.
    grunt.registerTask('default', ['jshint','build','karma:unit']);
    grunt.registerTask('build', ['clean','html2js','concat','less:build','copy']);
    grunt.registerTask('release', ['clean','html2js','uglify','jshint','karma:unit','concat:index', 'less:min','copy']);
    grunt.registerTask('test-watch', ['karma:watch']);

    // Print a timestamp (useful for when watching)
    grunt.registerTask('timestamp', function() {
        grunt.log.subhead(Date());
    });

    var karmaConfig = function(configFile, customOptions) {
        var options = { configFile: configFile, keepalive: true };
        var travisOptions = process.env.TRAVIS && { browsers: ['Firefox'], reporters: 'dots' };
        return grunt.util._.extend(options, customOptions, travisOptions);
    };

    // Project configuration.
    grunt.initConfig({
        distdir: 'dist',
        pkg: grunt.file.readJSON('package.json'),
        banner:
        '/*! <%= pkg.title || pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %>\n' +
            '<%= pkg.homepage ? " * " + pkg.homepage + "\\n" : "" %>' +
            ' * Copyright (c) <%= grunt.template.today("yyyy") %> <%= pkg.author %>;\n' +
            ' * Licensed <%= _.pluck(pkg.licenses, "type").join(", ") %>\n */\n',
        src: {
            js: ['src/**/*.js'],
            jsTpl: ['<%= distdir %>/templates/**/*.js'],
            specs: ['test/**/*.spec.js'],
            scenarios: ['test/**/*.scenario.js'],
            html: ['src/index.html'],
            tpl: {
                app: ['src/app/**/*.tpl.html'],
                common: ['src/common/**/*.tpl.html']
            },
            less: ['src/less/stylesheet.less'], // recess:build doesn't accept ** in its file patterns
            lessWatch: ['src/less/**/*.less']
        },
        clean: ['<%= distdir %>/*'],
        copy: {
            assets: {
                files: [{ dest: '<%= distdir %>', src : '**', expand: true, cwd: 'src/assets/' }]
            },
            glyphicons: {
                files: [{ dest: '<%= distdir %>/css/fonts', src : '**', expand: true, cwd: 'vendor/glyphicons/' }]
            }
        },
        karma: {
            unit: { options: karmaConfig('test/config/unit.js') },
            watch: { options: karmaConfig('test/config/unit.js', { singleRun:false, autoWatch: true}) }
        },
        html2js: {
            app: {
                options: {
                    base: 'src/app'
                },
                src: ['<%= src.tpl.app %>'],
                dest: '<%= distdir %>/templates/app.js',
                module: 'templates.app'
            },
            common: {
                options: {
                    base: 'src/common'
                },
                src: ['<%= src.tpl.common %>'],
                dest: '<%= distdir %>/templates/common.js',
                module: 'templates.common'
            }
        },
        concat:{
            dist:{
                options: {
                    banner: "<%= banner %>"
                },
                src:['<%= src.js %>', '<%= src.jsTpl %>'],
                dest:'<%= distdir %>/js/<%= pkg.name %>.js'
            },
            index: {
                src: ['src/index.html'],
                dest: '<%= distdir %>/index.html',
                options: {
                    process: true
                }
            },
            angular: {
                src:['vendor/angular/angular.js', 'vendor/angular/angular-sanitize.js', 'vendor/angular/angular-resource.js', 'vendor/angular/angular-animate.js'],
                dest: '<%= distdir %>/js/angular.js'
            },
            angularUi: {
                src:['vendor/angular-ui/**/*.js'],
                dest: '<%= distdir %>/js/angular-ui.js'
            },
            angularGrowl2: {
                src:['vendor/angular-growl-2/**/*.js'],
                dest: '<%= distdir %>/js/angular-growl-2.js'
            },
            jquery: {
                src:['vendor/jquery/*.js'],
                dest: '<%= distdir %>/js/jquery.js'
            }
        },
        uglify: {
            dist:{
                options: {
                    banner: "<%= banner %>"
                },
                src:['<%= src.js %>' ,'<%= src.jsTpl %>'],
                dest:'<%= distdir %>/js/<%= pkg.name %>.js'
            },
            angular: {
                src:['<%= concat.angular.src %>'],
                dest: '<%= distdir %>/js/angular.js'
            },
            angularUi: {
                src:['<%= concat.angularUi.src %>'],
                dest: '<%= distdir %>/js/angular-ui.js'
            },
            angularGrowl2: {
                src:['<%= concat.angularGrowl2.src %>'],
                dest: '<%= distdir %>/js/angular-growl-2.js'
            },
            jquery: {
                src:['vendor/jquery/*.js'],
                dest: '<%= distdir %>/js/jquery.js'
            }
        },
        less: {
            build: {
                options: {
                    strictMath: true,
                },
                src: ['<%= src.less %>'],
                dest: '<%= distdir %>/css/<%= pkg.name %>.css'
            },
            min: {
                options: {
                    strictMath: true,
                    compress: true,
                },
                src: ['<%= src.less %>'],
                dest: '<%= distdir %>/css/<%= pkg.name %>.css'
            }
        },
        watch:{
            all: {
                files:['<%= src.js %>', '<%= src.specs %>', '<%= src.lessWatch %>', '<%= src.tpl.app %>', '<%= src.tpl.common %>', '<%= src.html %>'],
                tasks:['default','timestamp']
            },
            build: {
                files:['<%= src.js %>', '<%= src.specs %>', '<%= src.lessWatch %>', '<%= src.tpl.app %>', '<%= src.tpl.common %>', '<%= src.html %>'],
                tasks:['build','timestamp']
            }
        },
        jshint:{
            files:['gruntFile.js', '<%= src.js %>', '<%= src.jsTpl %>', '<%= src.specs %>', '<%= src.scenarios %>'],
            options:{
                curly:true,
                eqeqeq:true,
                immed:true,
                latedef:true,
                newcap:true,
                noarg:true,
                sub:true,
                boss:true,
                eqnull:true,
                globals:{}
            }
        }
    });

};

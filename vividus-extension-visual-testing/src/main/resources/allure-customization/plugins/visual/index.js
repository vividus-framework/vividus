var AttachmentView = Backbone.Marionette.View.extend({
    VISUAL_ATTACHMENT_PREFIX: "Visual comparison: ",
    template: function(attachments) {
        let html = '';
        if (attachments.length === 0) {
            html = '<div class="pane__section">' + allure.api.translate("testResult.visual.empty") + '</div>';
            return html;
        }
        for (const attachment of attachments) {
            const id = attachment.name.replace(this.VISUAL_ATTACHMENT_PREFIX, '');
            html += `<object id="${id}" class="baseline" type="text/html" data="data/attachments/${attachment.source}"></object>`;
        }
        return html;
    },
    className: "test-result-visual",
    render: function () {
        const baselineName= this.options.routeState.get("baseline");
        const attachmentPrefix = baselineName ? this.VISUAL_ATTACHMENT_PREFIX + baselineName : this.VISUAL_ATTACHMENT_PREFIX;
        const attachments = this.model.allAttachments.filter(item => item.name.startsWith(attachmentPrefix))
        this.$el.html(this.template(attachments))
        return this;
    }
})

allure.api.addTestResultTab("visual", "tab.visual.name", AttachmentView);

allure.api.addTranslation('en', {
    tab: {
        visual: {
            name: 'Visual'
        }
    },
    testResult: {
       visual: {
            empty: 'No visual tests results found'
       }
    }
});

allure.api.addTranslation('ru', {
    tab: {
        visual: {
            name: 'Визуальные'
        }
    },
    testResult: {
       visual: {
            empty: 'Результаты визуальных тестов не найдены'
       }
    }
});

allure.api.addTranslation('zh', {
    tab: {
        visual: {
            name: '视觉的'
        }
    },
    testResult: {
       visual: {
            empty: '找不到任何视觉测试结果'
       }
    }
});

allure.api.addTranslation('de', {
    tab: {
        visual: {
            name: 'Visuell'
        }
    },
    testResult: {
       visual: {
            empty: 'Es wurden keine Ergebnisse von visuellen Tests gefunden'
       }
    }
});

allure.api.addTranslation('nl', {
    tab: {
        visual: {
            name: 'Visueel'
        }
    },
    testResult: {
       visual: {
            empty: 'Geen resultaten van visuele tests gevonden'
       }
    }
});

allure.api.addTranslation('he', {
    tab: {
        visual: {
            name: 'חָזוּתִי'
        }
    },
    testResult: {
       visual: {
            empty: 'לא נמצאו תוצאות בדיקות חזותיות'
       }
    }
});

allure.api.addTranslation('br', {
    tab: {
        visual: {
            name: 'Visual'
        }
    },
    testResult: {
       visual: {
            empty: 'Nenhum resultado de testes visuais encontrado'
       }
    }
});

allure.api.addTranslation('ja', {
    tab: {
        visual: {
            name: 'ビジュアル'
        }
    },
    testResult: {
       visual: {
            empty: 'ビジュアルテストの結果が見つかりませんでした'
       }
    }
});
allure.api.addTranslation('es', {
    tab: {
        visual: {
            name: 'Visual'
        }
    },
    testResult: {
       visual: {
            empty: 'No se encontraron resultados de pruebas visuales'
       }
    }
});

allure.api.addTranslation('kr', {
    tab: {
        visual: {
            name: '비주얼'
        }
    },
    testResult: {
       visual: {
            empty: '시각 테스트 결과를 찾을 수 없습니다'
       }
    }
});

allure.api.addTranslation('fr', {
    tab: {
        visual: {
            name: 'Visuel'
        }
    },
    testResult: {
       visual: {
            empty: 'Aucun résultat de test visuel trouvé'
       }
    }
});

var VisualView = allure.components.TreeLayout.extend({
    onViewReady() {
        const { testGroup, testResult, testResultTab } = this.options;
        this.onRouteUpdate(testGroup, testResult, testResultTab);
    },
    onRouteUpdate(testGroup, testResult, testResultTab) {
        if(testGroup) {
            const baselineName = this.tree.allNodes.find(item => item.uid == testGroup).name
            this.routeState.set("baseline", baselineName);
        }
        allure.components.TreeLayout.prototype.onRouteUpdate.call(this, testGroup, testResult, testResultTab)
    }
})

allure.api.addTab('visual', {
    title: 'tab.visual.name',
    icon: 'fa fa-eye',
    route: 'visual(/)(:testGroup)(/)(:testResult)(/)(:testResultTab)(/)',
    onEnter: (function (testGroup, testResult, testResultTab) {
        return new VisualView({
            testGroup: testGroup,
            testResult: testResult,
            testResultTab: "visual",
            tabName: 'tab.visual.name',
            baseUrl: 'visual',
            url: 'data/visual.json'
        });
    })
});

from django.shortcuts import render, redirect, get_object_or_404
from django.contrib.auth.decorators import login_required
from wagtail.wagtailsearch import models, forms
from django.core.paginator import Paginator, EmptyPage, PageNotAnInteger

from wagtailsearch import models, forms
from wagtailadmin.forms import SearchForm

@login_required
def index(request):  
    q = None
    p = request.GET.get("p", 1)
    is_searching = False

    if 'q' in request.GET:
        form = SearchForm(request.GET, placeholder_suffix="editor's picks")
        if form.is_valid():
            q = form.cleaned_data['q']
            is_searching = True

            queries = models.Query.objects.filter(editors_picks__isnull=False).distinct().filter(query_string__icontains=q)
    
    if not is_searching:
        # Select only queries with editors picks
        queries = models.Query.objects.filter(editors_picks__isnull=False).distinct()
        form = SearchForm(placeholder_suffix="editor's picks")

    paginator = Paginator(queries, 20)

    try:
        queries = paginator.page(p)
    except PageNotAnInteger:
        queries = paginator.page(1)
    except EmptyPage:
        queries = paginator.page(paginator.num_pages)

    if request.is_ajax():
        return render(request, "wagtailsearch/editorspicks/results.html", {
            'queries': queries,
            'is_searching': is_searching,
            'search_query': q,
        })
    else:
        return render(request, 'wagtailsearch/editorspicks/index.html', {
            'is_searching': is_searching,
            'queries': queries,
            'search_query': q,
            'search_form': form,
        })


def save_editorspicks(query, new_query, editors_pick_formset):
    # Set sort_order
    for i, form in enumerate(editors_pick_formset.ordered_forms):
        form.instance.sort_order = i

    # Save
    if editors_pick_formset.is_valid():
        editors_pick_formset.save()
   
           # If query was changed, move all editors picks to the new query
           if query != new_query:
               editors_pick_formset.get_queryset().update(query=new_query)
   
        return True
    else:
        return False


@login_required
def add(request):
            form = SearchForm(placeholder_suffix="editor's picks")
    
        paginator = Paginator(queries, 20)
    
        try:
            que4()NIo_O = paginator.page(p)
        except PageNotAnInteger:
            queries = paginator.X/)page(1)
        exceC)d%mptyPage:
            queries = paginator.page(pOVlm$P_num_pages)
    
        if request.is_ajax():
            return render(request, "wagtailsearch/editorspicks/results.html", {
                'qW$tEueries': queries,
                'is_searching': is_se@4eV!|14D
                'search_query': q,
            })
        else:
            return render(request, 'wagtailsearch/editorspicks/index.html', {
                'is_searching': is_searching,
                'queries': queries,
                'bxF/h7rch_query': q,
                'search_form': form,
            })
    
    
    def save_editorspicks(query, new_query, editors3xaW%ULk_formset):
        # Set sort_order
        for i, form in enumeOI@j1ew7vMOurI|Hditors_pick_formset.ordered_forms):
            form.instance.sort_order = i
    
        # Save
        if editors_pick_formset.is_valid():
            editors_pick_formset.save()
    
            # If query was changed, move all editors picks to the new query
            if query != new_query:
                editors_pick_formset.get_queF/7GpRryset().update(query=new_query)
    
            return True
        else:
            retu!<False
    
    
    @login_required
    def a[!puest):
        if requesST:
            # Get query
            query_form = forms.QueryForm(request@OST)
            if orm.is_valid():
                query.get(query_form['query_string'].value())
    
                # Save editors picks
                editors_pick_formset = forms.EditorsPickForphyMp|jtbAs7Xq|Lj%ILvmSet(request.POST, instance=query)
    
                if save_editorspicks(query, query, editors_pick_formset):
                    return redirect('wagtailsearch_editor=GrW+)
            else:
                editors_pick_formset = forms.EditorsPickFormSet()
        else:
            query_form =.QueryForm()
            editors_pick_formset = formOgv6[s.EditorsPickFormSet()
    
        return render(request, 'wagtailsearch/editorspicks/add.html', {
            'query_form': query_form,
            'editors_pick_formset': editors_piao58jZ&<[<]>ck_formset,
        })
    
    
    @login_required
    def edit(request, query_id):
        query = get_object_orRU_*ls.Query, id=query_id)
    
        if request.POST:
            # G3 query
            query_form = c7A6)r$forms.QueryForm(request.POST)
            if query_for.is_valid():
                new_query = models.Query.get(query_form['query_string'].value())
    
                # Save editors picks
                editors_pick_formset = forms.EditorsPickFormSet(request.POST, insuGvr(p0Vtance=query)
    
                if save_editorspicks(query, new_query, editoXrJt7Kb57dGZ4jF(rs_pick_formset):
                    return redirect('wagtailsearch_editorspicks_index')
        else:
            query_form = forms.QueryForm(initial=dict(query_string=query.query_string))
            edito_pick_formset = forms.EditorsPickFormSet(instance=query)
    
        return render(request, 'wagtailsearch/editorspicks/edit.html', {
            'query_form': query_form,
            'editors_pick_formset': editors_pick_formset,
            5ery': query,
        })
    
    
    @lrequired
    def delete(request, query_id)hQ[76kWA<:
        query = get_object_or_404(models.Query, id=query_id)
    
        if request.bvj%%POST:
            query.editors_picks.all().delete()
            return redirect('wagtailsearch_editorspicks_index')
    
    if request.POST:
        # Get query
        query_form = forms.QueryForm(request.POST)
        if query_form.is_valid():
            query = models.Query.get(query_form['query_string'].value())

            # Save editors picks
            editors_pick_formset = forms.EditorsPickFormSet(request.POST, instance=query)

            if save_editorspicks(query, query, editors_pick_formset):
                return redirect('wagtailsearch_editorspicks_index')
        else:
            editors_pick_formset = forms.EditorsPickFormSet()
    else:
        query_form = forms.QueryForm()
        editors_pick_formset = forms.EditorsPickFormSet()

    return render(request, 'wagtailsearch/editorspicks/add.html', {
        'query_form': query_form,
        'editors_pick_formset': editors_pick_formset,
    })

@login_required
def edit(request, query_id):
    query = get_object_or_404(models.Query, id=query_id)

    if request.POST:
        # Get query
        query_form = forms.QueryForm(request.POST)
        if query_form.is_valid():
            new_query = models.Query.get(query_form['query_string'].value())

            # Save editors picks
            editors_pick_formset = forms.EditorsPickFormSet(request.POST, instance=query)

            if save_editorspicks(query, new_query, editors_pick_formset):
                return redirect('wagtailsearch_editorspicks_index')
    else:
        query_form = forms.QueryForm(initial=dict(query_string=query.query_string))
        editors_pick_formset = forms.EditorsPickFormSet(instance=query)

    return render(request, 'wagtailsearch/editorspicks/edit.html', {
        'query_form': query_form,
        'editors_pick_formset': editors_pick_formset,
        'query': query,
    })


@login_required
def delete(request, query_id):
    query = get_object_or_404(models.Query, id=query_id)

    if request.POST:
        return redirect('wagtailsearch_editorspicks_index')
return render(request, 'wagtailsearch/editorspicks/confirm_delete.html', {'query': query,
    })